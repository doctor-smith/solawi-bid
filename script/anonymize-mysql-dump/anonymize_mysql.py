#!/usr/bin/env python3
"""Local, stdlib-only anonymizer for conventional mysqldump SQL files.

Intentionally fails closed for unreviewed text columns and SQL forms. This is
pseudonymisation, not a certification of irreversibility or GDPR compliance.
"""
from __future__ import annotations

import argparse
import datetime as dt
import fnmatch
import gzip
import hashlib
import hmac
import json
import os
from pathlib import Path
import re
import secrets
import sys
import tempfile
from collections import Counter
from dataclasses import dataclass
from typing import Iterator

IDENT = r'(?:`(?:``|[^`])+`|[A-Za-z_][A-Za-z0-9_$]*)'
QUALIFIED = rf'{IDENT}(?:\s*\.\s*{IDENT})?'
CREATE_RE = re.compile(rf'\bCREATE\s+(?:TEMPORARY\s+)?TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(?P<table>{QUALIFIED})\s*\(', re.I)
INSERT_RE = re.compile(rf'\A\s*(?P<verb>INSERT(?:\s+IGNORE)?|REPLACE)\s+INTO\s+(?P<table>{QUALIFIED})\s*(?P<rest>.*)\Z', re.I | re.S)
UNKNOWN_TEXT_TYPES = ('CHAR', 'TEXT', 'ENUM', 'SET', 'JSON', 'BLOB', 'BINARY', 'VARBINARY', 'GEOMETRY')
SAFE_TEXT_NAMES = {
    'status', 'state', 'type', 'kind', 'category', 'currency', 'locale',
    'language', 'lang', 'role', 'unit', 'timezone', 'time_zone', 'mode',
    'frequency', 'interval', 'color', 'colour', 'format', 'version',
    'action', 'method', 'source', 'target', 'provider', 'event_type',
    'payment_method', 'transaction_type', 'account_type', 'country_code',
    'country', 'iso_code', 'code', 'slug', 'reference_type',
}
REDACT_PATTERNS = ('note', 'notes', 'comment', 'comments', 'remark', 'message',
    'description', 'free_text', 'freetext', 'body', 'content', 'memo',
    'subject', 'purpose', 'reference_text', 'booking_text', 'verwendungszweck',
    'nachricht', 'bemerkung', 'beschreibung', 'reason_text', 'payload',
    'metadata', 'json', 'biography', 'bio', 'signature', 'attachment',
    'document', 'file_data', 'file_name', 'filename')
SECRET_PATTERNS = ('password', 'passwd', 'passphrase', 'secret', 'token',
    'api_key', 'apikey', 'private_key', 'public_key', 'salt', 'hash',
    'credential', 'session', 'auth_code', 'verification_code', 'pin',
    'otp', 'reset_key', 'recovery_code', 'access_key')
EMAIL_PATTERNS = ('email', 'e_mail', 'mail_address', 'mailadresse', 'emailadresse')
USERNAME_PATTERNS = ('username', 'user_name', 'login', 'login_name', 'benutzername', 'nickname', 'nick_name')
IBAN_PATTERNS = ('iban',)
BIC_PATTERNS = ('bic', 'swift', 'swift_code')
CREDITOR_PATTERNS = ('creditor_id', 'creditorid', 'glaeubiger_id', 'glaeubigerid', 'gläubigerid', 'sepa_creditor')
PHONE_PATTERNS = ('phone', 'telephone', 'telefon', 'mobile', 'handy', 'fax', 'telefonnummer', 'phone_number')
ADDRESS_PATTERNS = ('address', 'adresse', 'street', 'strasse', 'straße', 'house_number', 'housenumber', 'hausnummer', 'city', 'stadt', 'ort', 'postal', 'postcode', 'zip_code', 'zipcode', 'plz')
DOB_PATTERNS = ('birthday', 'birth_date', 'birthdate', 'date_of_birth', 'dob', 'geburtsdatum', 'geburtstag')
NAME_FIRST = ('first_name', 'firstname', 'given_name', 'givenname', 'vorname')
NAME_LAST = ('last_name', 'lastname', 'family_name', 'familyname', 'surname', 'nachname')
NAME_FULL = ('full_name', 'fullname', 'display_name', 'displayname', 'contact_name', 'kontoinhaber', 'account_holder', 'owner_name')
URL_PATTERNS = ('website', 'homepage', 'url', 'web_url', 'profile_url')
IP_PATTERNS = ('ip', 'ip_address', 'ipaddress', 'remote_addr', 'client_ip')
FEMALE = ('Anna', 'Laura', 'Mia', 'Sophie', 'Lena', 'Nina', 'Eva', 'Clara')
MALE = ('Max', 'Paul', 'Jonas', 'Leon', 'Felix', 'Lukas', 'Tim', 'Jan')
LAST = ('Muster', 'Beispiel', 'Testmann', 'Demonstrator', 'Probe', 'Modell', 'Testfeld', 'Mustermann')
CITIES = ('Teststadt', 'Musterhausen', 'Beispielstadt', 'Demodorf')

class DumpError(Exception):
    pass


def normalize_identifier(identifier: str) -> str:
    return identifier.strip().replace('`', '').split('.')[-1].strip().lower()


def chunks(sql: str, delim: str = ',') -> list[str]:
    """Split SQL at a delimiter outside MySQL quoted strings/backticks/parens."""
    parts, pos, depth, quote, i = [], 0, 0, None, 0
    while i < len(sql):
        ch = sql[i]
        if quote:
            if ch == '\\':
                i += 2
                continue
            if ch == quote:
                if i + 1 < len(sql) and sql[i + 1] == quote:
                    i += 2
                    continue
                quote = None
        elif ch in "'\"`":
            quote = ch
        elif ch == '(':
            depth += 1
        elif ch == ')':
            depth -= 1
            if depth < 0:
                raise DumpError('Unbalanced parentheses')
        elif ch == delim and depth == 0:
            parts.append(sql[pos:i].strip())
            pos = i + 1
        i += 1
    if quote or depth:
        raise DumpError('Unclosed SQL quote or parentheses')
    parts.append(sql[pos:].strip())
    return parts


def find_end_parenthesis(sql: str, start: int) -> int:
    depth, quote, i = 0, None, start
    while i < len(sql):
        ch = sql[i]
        if quote:
            if ch == '\\':
                i += 2
                continue
            if ch == quote:
                if i + 1 < len(sql) and sql[i + 1] == quote:
                    i += 2
                    continue
                quote = None
        elif ch in "'\"`":
            quote = ch
        elif ch == '(':
            depth += 1
        elif ch == ')':
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise DumpError('CREATE TABLE has unclosed parentheses')


def unquote(raw: str) -> str | None:
    v = raw.strip()
    if re.fullmatch(r"(?:0x[0-9A-Fa-f]+|[Xx]'[0-9A-Fa-f]+')", v):
        return bytes.fromhex(v[2:] if v.startswith(('0x','0X')) else v[2:-1]).decode('utf-8', 'surrogateescape')
    v = re.sub(r'^_binary\s*(?=[\'\"])', '', v, flags=re.I)
    if v.upper() == 'NULL':
        return None
    if len(v) >= 2 and v[0] in "'\"" and v[-1] == v[0]:
        body = v[1:-1]
        out, i = [], 0
        esc = {'0': '\x00', 'b': '\b', 'n': '\n', 'r': '\r', 't': '\t', 'Z': '\x1a', '\\': '\\', "'": "'", '"': '"'}
        while i < len(body):
            ch = body[i]
            if ch == '\\' and i + 1 < len(body):
                out.append(esc.get(body[i + 1], body[i + 1]))
                i += 2
            elif ch == v[0] and i + 1 < len(body) and body[i + 1] == ch:
                out.append(ch)
                i += 2
            else:
                out.append(ch)
                i += 1
        return ''.join(out)
    # Non-quoted literals are not decoded. Callers only transform known types.
    return v


def quoted(value: str) -> str:
    esc = {'\\': '\\\\', "'": "\\'", '\x00': '\\0', '\b': '\\b', '\n': '\\n', '\r': '\\r', '\t': '\\t', '\x1a': '\\Z'}
    return "'" + ''.join(esc.get(c, c) for c in value) + "'"


def open_dump(path: Path):
    return gzip.open(path, 'rt', encoding='utf-8', errors='surrogateescape', newline='') if path.suffix == '.gz' else path.open('r', encoding='utf-8', errors='surrogateescape', newline='')


def iter_statements(file, max_size: int = 128 * 1024 * 1024) -> Iterator[str]:
    """Split by semicolon, respecting strings, backticks and SQL comments."""
    buff, size = [], 0
    quote, block, line_comment = None, False, False
    for line in file:
        start, i = 0, 0
        while i < len(line):
            ch = line[i]
            nxt = line[i + 1] if i + 1 < len(line) else ''
            if line_comment:
                i += 1
                continue
            if block:
                if ch == '*' and nxt == '/':
                    block = False
                    i += 2
                    continue
            elif quote:
                if ch == '\\':
                    i += 2
                    continue
                if ch == quote:
                    if ch in "'\"`" and nxt == quote:
                        i += 2
                        continue
                    quote = None
            elif ch in "'\"`":
                quote = ch
            elif ch == '/' and nxt == '*':
                block = True
                i += 2
                continue
            elif ch == '#':
                line_comment = True
            elif ch == '-' and nxt == '-' and (i + 2 == len(line) or line[i + 2].isspace()):
                line_comment = True
            elif ch == ';':
                frag = line[start:i + 1]
                buff.append(frag)
                size += len(frag)
                if size > max_size:
                    raise DumpError('SQL statement too large; export with mysqldump --skip-extended-insert')
                yield ''.join(buff)
                buff, size = [], 0
                start = i + 1
            i += 1
        remainder = line[start:]
        buff.append(remainder)
        size += len(remainder)
        if size > max_size:
            raise DumpError('SQL statement too large; export with mysqldump --skip-extended-insert')
        line_comment = False
    if quote or block:
        raise DumpError('Unterminated quote or comment in SQL dump')
    if buff and ''.join(buff).strip():
        yield ''.join(buff)


def strip_comments(sql: str) -> tuple[str, str]:
    i = 0
    while i < len(sql):
        m = re.match(r'\s+', sql[i:])
        if m:
            i += m.end()
            continue
        if sql.startswith('--', i) and (i + 2 >= len(sql) or sql[i + 2].isspace()) or sql.startswith('#', i):
            end = sql.find('\n', i)
            i = len(sql) if end == -1 else end + 1
            continue
        # versioned MySQL comments are executable SQL; keep as an opaque statement
        if sql.startswith('/*', i) and not sql.startswith('/*!', i):
            end = sql.find('*/', i + 2)
            if end < 0:
                raise DumpError('Unterminated comment')
            i = end + 2
            continue
        break
    return sql[:i], sql[i:]


@dataclass
class Column:
    name: str
    dtype: str


@dataclass
class Insert:
    table: str
    columns: list[str] | None
    rows: list[list[str]]
    prefix: str
    trailing: str


def parse_create(sql: str) -> tuple[str, list[Column]] | None:
    m = CREATE_RE.search(sql)
    if not m:
        return None
    end = find_end_parenthesis(sql, m.end() - 1)
    items = chunks(sql[m.end():end])
    cols = []
    for item in items:
        x = re.match(rf'^\s*(?P<field>{IDENT})\s+(?P<type>[A-Za-z]+)', item)
        if not x:
            continue
        field, typ = normalize_identifier(x.group('field')), x.group('type').upper()
        if field in ('primary', 'unique', 'key', 'constraint', 'foreign', 'index', 'check', 'fulltext', 'spatial'):
            continue
        cols.append(Column(field, typ))
    if not cols:
        raise DumpError(f'Cannot read CREATE TABLE for {normalize_identifier(m.group("table"))}')
    return normalize_identifier(m.group('table')), cols


def parse_insert(sql: str) -> Insert | None:
    m = INSERT_RE.match(sql)
    if not m:
        return None
    table = normalize_identifier(m.group('table'))
    rest = m.group('rest')
    columns = None
    head = 0
    if rest.lstrip().startswith('('):
        head = len(rest) - len(rest.lstrip())
        end = find_end_parenthesis(rest, head)
        columns = [normalize_identifier(c) for c in chunks(rest[head + 1:end])]
        head = end + 1
    val = re.match(r'\s*VALUES\b', rest[head:], re.I)
    if not val:
        raise DumpError(f'Unsupported INSERT form for {table}; expected VALUES')
    offset = head + val.end()
    rowstr = rest[offset:]
    # Keep semicolon and trailing whitespace separately.
    endsemi = rowstr.rfind(';')
    if endsemi < 0:
        raise DumpError(f'INSERT in {table} has no semicolon')
    data, trail = rowstr[:endsemi], rowstr[endsemi:]
    raw_rows = chunks(data)
    rows = []
    for raw in raw_rows:
        v = raw.strip()
        if not (v.startswith('(') and v.endswith(')')):
            raise DumpError(f'Unsupported row value syntax in {table}')
        rows.append(chunks(v[1:-1]))
    return Insert(table, columns, rows, sql[:m.start('rest') + offset], trail)


def digest(key: bytes, label: str, source: str) -> bytes:
    return hmac.new(key, (label + '\0' + source.strip().lower()).encode('utf-8', 'surrogateescape'), hashlib.sha256).digest()


def num(key: bytes, label: str, source: str, modulo: int) -> int:
    return int.from_bytes(digest(key, label, source)[:8], 'big') % modulo


def check_digits(country: str, bban: str) -> str:
    data = (bban + country + '00').upper()
    digits = ''.join(str(ord(c) - 55) if 'A' <= c <= 'Z' else c for c in data)
    rem = 0
    for ch in digits:
        if not ch.isdigit():
            raise DumpError('Invalid character in IBAN/Creditor-ID construction')
        rem = (rem * 10 + int(ch)) % 97
    return str(98 - rem).zfill(2)


def fake_iban(key: bytes, original: str) -> str:
    # BLZ 00000000 is deliberately non-routable; check digits remain valid.
    bban = '00000000' + f'{num(key, "iban", original, 10**10):010d}'
    return 'DE' + check_digits('DE', bban) + bban


def fake_creditor(key: bytes, original: str) -> str:
    national = f'{num(key, "creditor", original, 10**11):011d}'
    bban = 'ZZZ' + national
    return 'DE' + check_digits('DE', bban) + bban


def fake_bic(key: bytes, original: str) -> str:
    ab = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    v = digest(key, 'bic', original)
    return 'TESTDE' + ab[v[0] % len(ab)] + ab[v[1] % len(ab)] + 'XXX'


def classify(table: str, col: str, dtype: str, config: dict) -> str:
    rules = {k.lower(): v.lower() for k, v in config.get('rules', {}).items()}
    for pat in (f'{table}.{col}', f'*.{col}'):
        if pat in rules:
            return rules[pat]
    for pattern in config.get('allow_columns', []):
        if fnmatch.fnmatch(f'{table}.{col}', pattern.lower()):
            return 'keep'
    name = col.lower()
    # Identifiers and foreign keys stay intact to preserve referential integrity.
    if name in ('mandate_reference', 'mandate_reference_prefix', 'collection_key', 'end_to_end_id',
                'end_to_end_id_prefix', 'message_id'):
        return 'reference'
    if name in ('webling_id',):
        return 'external_id'
    if name in ('id', 'uuid') or name.endswith(('_id', '_uuid')):
        # Security and bank IDs must be handled before this ID fallback.
        if any(s in name for s in CREDITOR_PATTERNS):
            return 'creditor'
        if any(s in name for s in SECRET_PATTERNS):
            return 'secret'
        if name in ('tax_id', 'national_id', 'identity_id', 'passport_id'):
            return 'secret'
        return 'keep'
    if name in ('account_number', 'accountnumber', 'kontonummer', 'bank_account_number', 'routing_number', 'bankleitzahl', 'blz'):
        return 'bank_number'
    if any(p in name for p in IBAN_PATTERNS): return 'iban'
    if any(p in name for p in BIC_PATTERNS): return 'bic'
    if any(p in name for p in CREDITOR_PATTERNS): return 'creditor'
    if any(p in name for p in EMAIL_PATTERNS): return 'email'
    if any(p in name for p in USERNAME_PATTERNS): return 'username'
    if any(p in name for p in SECRET_PATTERNS): return 'secret'
    if any(p in name for p in DOB_PATTERNS): return 'dob'
    if any(p in name for p in NAME_FIRST): return 'first_name'
    if any(p in name for p in NAME_LAST): return 'last_name'
    if name in NAME_FULL or name.endswith('_full_name') or name.endswith('_contact_name'): return 'full_name'
    if any(p in name for p in PHONE_PATTERNS): return 'phone'
    if any(p in name for p in ADDRESS_PATTERNS): return 'address'
    if name in ('sex', 'gender', 'geschlecht'): return 'gender'
    if name in ('tax_number', 'tax_no', 'vat_number', 'vat_id', 'steuernummer', 'ust_id'): return 'secret'
    if any(p in name for p in IP_PATTERNS) and (name == 'ip' or name.startswith(('ip_', 'client_ip', 'remote_addr'))): return 'ip'
    if any(p in name for p in URL_PATTERNS) and (name == 'url' or name.endswith(('_url', 'website', 'homepage'))): return 'url'
    if any(p in name for p in REDACT_PATTERNS): return 'redact'
    if name in SAFE_TEXT_NAMES or name.endswith(('_status', '_state', '_type', '_code', '_currency')):
        return 'keep'
    if any(t in dtype.upper() for t in UNKNOWN_TEXT_TYPES):
        return 'unknown'
    if dtype == '?':
        return 'unknown'
    return 'keep'


def value_to_sql(value: str, dtype: str, original_raw: str) -> str:
    if original_raw.strip().upper() == 'NULL' or value is None:
        return 'NULL'
    if dtype.upper() in ('INT', 'INTEGER', 'BIGINT', 'SMALLINT', 'TINYINT', 'MEDIUMINT', 'DECIMAL', 'NUMERIC', 'FLOAT', 'DOUBLE', 'REAL'):
        # All replaced numeric values must be representable as integers.
        return str(int.from_bytes(hashlib.sha256(value.encode('utf-8')).digest()[:2], 'big') % 100000)
    return quoted(value)


class Anonymizer:
    def __init__(self, key: bytes, config: dict):
        self.key = key
        self.config = config
        self.tables: dict[str, list[Column]] = {}
        self.mailmap: dict[str, str] = {}
        self.loginmap: dict[str, str] = {}
        self.totals: Counter[str] = Counter()
        self.schema_unknown: list[str] = []
        self.user_tables = {x.lower() for x in config.get('user_tables', ['user', 'users', 'account', 'accounts'])}
        self.id_column = str(config.get('user_id_column', 'id')).lower()
        self._user_seq = 0
        self.external_ids: dict[str, str] = {}
        self.external_ids_used: set[str] = set()

    def describe_columns(self, table: str, names: list[str] | None) -> list[Column]:
        meta = self.tables.get(table)
        if names is None:
            if meta is None:
                raise DumpError(f'INSERT INTO {table} has no columns and its CREATE TABLE was not found')
            return meta
        by_name = {c.name: c for c in meta or []}
        return [by_name.get(n, Column(n, '?')) for n in names]

    def prepass(self, path: Path):
        with open_dump(path) as f:
            for item in iter_statements(f):
                _, st = strip_comments(item)
                if not st.strip():
                    continue
                if cr := parse_create(st):
                    self.tables[cr[0]] = cr[1]
                    continue
                if not re.match(r'\s*(INSERT|REPLACE)\b', st, re.I):
                    continue
                ins = parse_insert(st)
                if ins is None:
                    continue
                cols = self.describe_columns(ins.table, ins.columns)
                if any(len(row) != len(cols) for row in ins.rows):
                    raise DumpError(f'Column/value count mismatch in {ins.table}')
                if ins.table not in self.user_tables:
                    continue
                names = [c.name for c in cols]
                for row in ins.rows:
                    if self.id_column not in names:
                        raise DumpError(f'User table {ins.table} missing usable {self.id_column}; configure user_id_column')
                    val = unquote(row[names.index(self.id_column)])
                    if not val:
                        raise DumpError(f'Missing user ID in {ins.table}')
                    # Binary UUIDs cannot be safely represented in an email address.
                    # Give them consecutive numbers within this dump instead.
                    if any(c.name == self.id_column and c.dtype in ('BINARY', 'VARBINARY') for c in cols):
                        self._user_seq += 1
                        suffix = str(self._user_seq)
                    else:
                        suffix = re.sub('[^A-Za-z0-9_-]', '-', str(val))
                    if not suffix:
                        raise DumpError(f'User ID cannot be used in email alias for {ins.table}')
                    alias = 'user-' + suffix
                    email = alias + '@solyton.org'
                    for col in cols:
                        if classify(ins.table, col.name, col.dtype, self.config) not in ('email', 'username'):
                            continue
                        original = unquote(row[names.index(col.name)])
                        if not original:
                            continue
                        target_map = self.mailmap if '@' in original or classify(ins.table, col.name, col.dtype, self.config) == 'email' else self.loginmap
                        target = email  # Both email and username use user-{id}@solyton.org
                        k = original.strip().lower()
                        if k in target_map and target_map[k] != target:
                            raise DumpError(f'Conflicting user identity mapping in {ins.table} (duplicate email/login)')
                        target_map[k] = target

    def audit(self) -> dict:
        out = {}
        for table, columns in sorted(self.tables.items()):
            out[table] = {c.name: classify(table, c.name, c.dtype, self.config) for c in columns}
        unknown = [f'{table}.{col}' for table, rules in out.items() for col, category in rules.items() if category == 'unknown']
        return {'tables': out, 'unknown_columns': unknown, 'user_identity_mappings': len(self.mailmap), 'note': 'No real row values are included in this report.'}

    def anon(self, table: str, col: Column, raw: str) -> str:
        cat = classify(table, col.name, col.dtype, self.config)
        if cat == 'keep': return raw
        original = unquote(raw)
        if original is None or original == '': return raw
        if cat == 'unknown':
            if original_raw_is_safe_default(raw):
                return raw
            raise DumpError(f'Unreviewed text column {table}.{col.name}; add rules or allow_columns to config')
        self.totals[cat] += 1
        n = lambda typ, size: num(self.key, typ, original, size)
        if cat == 'email':
            result = self.mailmap.get(original.strip().lower()) or 'user-' + digest(self.key, 'email', original).hex()[:16] + '@solyton.org'
        elif cat == 'username':
            result = self.loginmap.get(original.strip().lower()) or self.mailmap.get(original.strip().lower())
            if not result:
                token = digest(self.key, 'username', original).hex()[:12]
                result = 'user-' + token + '@solyton.org'
        elif cat == 'iban': result = fake_iban(self.key, original)
        elif cat == 'reference': result = 'REF-' + digest(self.key, 'reference', original).hex()[:20]
        elif cat == 'external_id':
            if original == '0': return raw  # Preserved sentinel, not an external identifier.
            if original not in self.external_ids:
                candidate = str(1 + n('external_id', 2_000_000_000))
                if candidate in self.external_ids_used:
                    raise DumpError('External ID hash collision; use a different ANONYMIZER_KEY')
                self.external_ids[original] = candidate
                self.external_ids_used.add(candidate)
            result = self.external_ids[original]
        elif cat == 'bank_number': result = f'{n("bank_number", 10**10):010d}'
        elif cat == 'bic': result = fake_bic(self.key, original)
        elif cat == 'creditor': result = fake_creditor(self.key, original)
        elif cat == 'first_name': result = (FEMALE + MALE)[n('firstname', 16)]
        elif cat == 'last_name': result = LAST[n('lastname', len(LAST))]
        elif cat == 'full_name': result = FEMALE[n('full', len(FEMALE))] + ' ' + LAST[n('full_last', len(LAST))]
        elif cat == 'phone': result = '+4900' + f'{n("phone", 10**8):08d}'
        elif cat == 'organization_name': result = 'Organisation-' + str(1+n('organization', 10**8))
        elif cat == 'region': result = 'Musterland'
        elif cat == 'address':
            if any(s in col.name for s in ('zip', 'post', 'plz')): result = f'{n("zip", 90000) + 10000:05d}'
            elif any(s in col.name for s in ('city', 'stadt', 'ort')): result = CITIES[n('city', len(CITIES))]
            elif any(s in col.name for s in ('house_number', 'housenumber', 'hausnummer')): result = str(1 + n('house', 99))
            else: result = f'Musterstraße {1 + n("street", 99)}'
        elif cat == 'dob':
            try:
                date = dt.date.fromisoformat(original[:10])
                result = str(date + dt.timedelta(days=n('dob', 541) - 270)) + original[10:]
            except ValueError:
                result = '1990-01-01'
        elif cat == 'gender':
            # Keep the vocabulary allowed by common MySQL ENUM columns.
            if original.lower() in ('m', 'f', 'd', 'w'):
                result = ('m', 'f', 'd')[n('gender', 3)] if original.lower() != 'w' else ('m', 'w', 'd')[n('gender', 3)]
            elif original.lower() in ('male', 'female', 'other', 'diverse'):
                result = ('male', 'female', 'other')[n('gender', 3)]
            else:
                raise DumpError(f'Unknown gender vocabulary in {table}.{col.name}; configure this field explicitly')
        elif cat == 'ip': result = f'192.0.2.{1+n("ip", 254)}'  # reserved documentation range
        elif cat == 'url': result = f'https://example.org/{digest(self.key,"url",original).hex()[:12]}'
        elif cat == 'secret' and col.dtype.upper() in ('BINARY', 'VARBINARY', 'BLOB', 'TINYBLOB', 'MEDIUMBLOB', 'LONGBLOB'):
            # Preserve the length and binary type, but invalidate the existing token.
            source_bytes = original.encode('utf-8', 'surrogateescape')
            length = len(source_bytes)
            if length <= 0 or length > 1000000:
                raise DumpError(f'Unsupported secret binary length in {table}.{col.name}')
            seed = digest(self.key, 'binary_secret', original)
            stream = b''.join(hmac.new(seed, j.to_bytes(4,'big'), hashlib.sha256).digest() for j in range((length+31)//32))
            return '0x' + stream[:length].hex()
        elif cat in ('redact', 'secret'): result = '[REDACTED]'
        else: raise DumpError(f'Unknown rule type {cat!r} for {table}.{col.name}')
        return value_to_sql(result, col.dtype, raw)

    def transform(self, path: Path, out: Path):
        count_rows, count_statements = 0, 0
        fd, temp_name = tempfile.mkstemp(prefix='.anonymized-', suffix='.sql', dir=out.parent)
        os.chmod(temp_name, 0o600)
        try:
            with os.fdopen(fd, 'w', encoding='utf-8', errors='surrogateescape', newline='') as target, open_dump(path) as source:
                for statement in iter_statements(source):
                    pre, st = strip_comments(statement)
                    if not st.strip():
                        # Ordinary dump comments can contain operator names, paths,
                        # hostnames or even private data. Do not export them.
                        target.write('\n' * max(1, statement.count('\n')))
                        continue
                    if re.match(r'\s*(INSERT|REPLACE)\b', st, re.I):
                        ins = parse_insert(st)
                        cols = self.describe_columns(ins.table, ins.columns)
                        if any(len(row) != len(cols) for row in ins.rows):
                            raise DumpError(f'Value/column mismatch in {ins.table}')
                        newrows = []
                        for row in ins.rows:
                            newrows.append('(' + ','.join(self.anon(ins.table, col, raw) for col, raw in zip(cols, row)) + ')')
                        target.write('\n' * pre.count('\n') + ins.prefix + ','.join(newrows) + ins.trailing)
                        count_rows += len(newrows)
                        count_statements += 1
                    else:
                        command = st.lstrip()
                        keyword = command.split(None, 1)[0].upper() if command else ''
                        # mysql's executable comments are executable SQL, not plain comments.
                        if command.startswith('/*!'):
                            if not re.match(r'/\*!\d{5,6}\s+(SET|ALTER)\b', command, re.I):
                                raise DumpError('Unsupported executable MySQL comment')
                        elif keyword not in ('CREATE', 'DROP', 'ALTER', 'LOCK', 'UNLOCK',
                                             'SET', 'USE', 'START', 'COMMIT', 'ROLLBACK',
                                             'BEGIN', 'SAVEPOINT', 'RELEASE'):
                            raise DumpError(f'Unsupported SQL statement: {keyword}')
                        elif keyword == 'CREATE' and not re.match(
                                r'CREATE\s+(?:TEMPORARY\s+)?(?:TABLE|DATABASE|SCHEMA)\b',
                                command, re.I):
                            raise DumpError('Unsupported CREATE statement')
                        target.write('\n' * pre.count('\n') + st)
            os.replace(temp_name, out)
        except Exception:
            try: os.remove(temp_name)
            except FileNotFoundError: pass
            raise
        return {'rows': count_rows, 'insert_statements': count_statements, 'replaced_values': dict(self.totals), 'output': str(out)}


def original_raw_is_safe_default(raw: str) -> bool:
    # No automatic "keep" of unknown nonempty textual values.
    return raw.strip().upper() == 'NULL' or raw.strip() in ("''", '""')


def main() -> int:
    p = argparse.ArgumentParser(description='Local, fail-closed MySQL mysqldump pseudonymizer')
    p.add_argument('dump', type=Path, help='Input .sql or .sql.gz (not modified)')
    p.add_argument('--output', '-o', type=Path, help='New output .sql (anonymize only)')
    p.add_argument('--config', type=Path, help='JSON rules and allow_columns')
    p.add_argument('--audit', action='store_true', help='Only inspect schema; no output dump')
    p.add_argument('--key-env', default='ANONYMIZER_KEY', help='Environment variable for secret mapping key')
    a = p.parse_args()
    try:
        if not a.dump.is_file(): raise DumpError(f'Input not found: {a.dump}')
        if not a.audit and not a.output: raise DumpError('Provide --output or use --audit')
        if a.output and a.output.resolve() == a.dump.resolve(): raise DumpError('Input and output must differ')
        if a.output and a.output.exists(): raise DumpError('Output exists: refusing to overwrite')
        cfg = json.loads(a.config.read_text('utf-8')) if a.config else {}
        key = os.environ.get(a.key_env)
        if not key and not a.audit:
            print('Note: no ANONYMIZER_KEY set; a one-time random key is used.', file=sys.stderr)
        worker = Anonymizer(key.encode('utf-8') if key else secrets.token_bytes(32), cfg)
        worker.prepass(a.dump)
        if a.audit:
            print(json.dumps(worker.audit(), ensure_ascii=False, indent=2))
        else:
            report = worker.transform(a.dump, a.output)
            print(json.dumps(report, ensure_ascii=False, indent=2))
            print('WARNING: This is pseudonymisation, not proof of anonymisation. Manually review before sharing.', file=sys.stderr)
        return 0
    except (DumpError, OSError, ValueError, json.JSONDecodeError) as ex:
        print('ERROR:', ex, file=sys.stderr)
        return 2


if __name__ == '__main__':
    raise SystemExit(main())
