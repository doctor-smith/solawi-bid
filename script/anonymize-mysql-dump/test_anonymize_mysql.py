"""Run with: python3 -m unittest -v test_anonymize_mysql.py"""
import os
from pathlib import Path
import re
import tempfile
import unittest
from anonymize_mysql import (
    Anonymizer, DumpError, chunks, check_digits, fake_bic, fake_creditor,
    fake_iban, iter_statements, parse_create, parse_insert, unquote,
)

KEY = b'TEST-KEY-FOR-FAKE-ONLY'
SAMPLE = """-- Synthetic fixture; not real private data
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (`id` INT PRIMARY KEY,`email` varchar(255),`username` varchar(50),`first_name` varchar(50),`last_name` varchar(50),`iban` varchar(34),`bic` varchar(11),`creditorId` varchar(18),`notes` text,`created_at` datetime);
INSERT INTO `users` VALUES (7,'demo@example.invalid','demo','Anna','Demo','DE89370400440532013000','COBADEFFXXX','DE98ZZZ09999999999','Please call demo@example.invalid','2024-01-01 12:00:00');
CREATE TABLE `payments` (`id` INT,`user_id` INT,`email` varchar(255),`amount` decimal(10,2),`status` varchar(10));
INSERT INTO `payments` (`id`,`user_id`,`email`,`amount`,`status`) VALUES (50,7,'demo@example.invalid',37.40,'paid');
"""


def mod97(value: str) -> int:
    rearranged = value[4:] + value[:4]
    sequence = ''.join(str(ord(c)-55) if c.isalpha() else c for c in rearranged)
    remainder = 0
    for ch in sequence:
        remainder = (remainder * 10 + int(ch)) % 97
    return remainder


class Tests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.src = Path(self.tmp.name) / 'in.sql'
        self.out = Path(self.tmp.name) / 'out.sql'

    def worker(self, sample=SAMPLE, config=None):
        self.src.write_text(sample, encoding='utf-8')
        worker = Anonymizer(KEY, config or {})
        worker.prepass(self.src)
        return worker

    def test_cross_table_email_and_integrity(self):
        worker = self.worker()
        report = worker.transform(self.src, self.out)
        s = self.out.read_text()
        self.assertEqual(report['rows'], 2)
        self.assertEqual(s.count('user-7@solyton.org'), 3)
        self.assertNotIn('demo@example.invalid', s)
        self.assertNotIn('Please call', s)
        self.assertIn('(50,7,\'user-7@solyton.org\',37.40,\'paid\')', s)
        self.assertNotIn('DE89370400440532013000', s)
        self.assertIn('2024-01-01 12:00:00', s)
        self.assertEqual(self.out.stat().st_mode & 0o777, 0o600)

    def test_comments_not_copied(self):
        example = '-- email operator@example.invalid\nCREATE TABLE `x` (`id` INT);\nINSERT INTO `x` VALUES (1);\n'
        w = self.worker(example)
        w.transform(self.src, self.out)
        self.assertNotIn('operator@example.invalid', self.out.read_text())

    def test_reject_procedures_and_unsupported_conditional_sql(self):
        for stmt in [
            "CREATE PROCEDURE leak() SELECT 'test@example.invalid';",
            '/*!50003 CREATE TRIGGER x BEFORE INSERT ON y FOR EACH ROW SET @a=1 */;',
        ]:
            with self.subTest(stmt=stmt):
                w = self.worker(stmt)
                with self.assertRaises(DumpError):
                    w.transform(self.src, self.out)
                self.assertFalse(self.out.exists())

    def test_iban_valid_checkdigits_and_norouting(self):
        x = fake_iban(KEY, 'DE89370400440532013000')
        self.assertEqual(len(x), 22)
        self.assertEqual(mod97(x), 1)
        self.assertEqual(x[4:12], '00000000')
        self.assertEqual(x, fake_iban(KEY, 'DE89370400440532013000'))
        self.assertNotEqual(x, fake_iban(KEY, 'DE11111111111111111111'))

    def test_bic_and_creditor_format(self):
        bic = fake_bic(KEY, 'COBADEFFXXX')
        creditor = fake_creditor(KEY, 'DE98ZZZ09999999999')
        self.assertRegex(bic, r'^TESTDE[A-Z0-9]{2}XXX$')
        self.assertRegex(creditor, r'^DE\d{2}ZZZ\d{11}$')
        self.assertEqual(mod97(creditor), 1)

    def test_unreviewed_text_fails_closed(self):
        w = self.worker("CREATE TABLE `x` (`id` int,`unknown_field` varchar(100));\nINSERT INTO `x` VALUES (1,'do not leak');\n")
        with self.assertRaisesRegex(DumpError, 'Unreviewed text'):
            w.transform(self.src, self.out)
        self.assertFalse(self.out.exists())

    def test_allowlist(self):
        w = self.worker("CREATE TABLE `x` (`id` int,`unknown_field` varchar(100));\nINSERT INTO `x` VALUES (1,'safe categorical');\n", {'allow_columns': ['x.unknown_field']})
        w.transform(self.src, self.out)
        self.assertIn('safe categorical', self.out.read_text())

    def test_literals_with_escaped_comma_quotes_and_semicolons(self):
        s = "INSERT INTO `x` VALUES (1,'a,b; \\'quote\\' and \\\\ slash'),(2,'hi');\n"
        st = list(iter_statements(iter([s])))
        ins = parse_insert(st[0])
        self.assertEqual(len(ins.rows), 2)
        self.assertEqual(unquote(ins.rows[0][1]), "a,b; 'quote' and \\ slash")

    def test_reject_update_and_only_write_on_success(self):
        w = self.worker('UPDATE users SET email=\'x@real.net\';\n')
        with self.assertRaisesRegex(DumpError, 'Unsupported SQL'):
            w.transform(self.src, self.out)
        self.assertFalse(self.out.exists())

    def test_non_utf8_binary_uuid_dump_round_trip(self):
        # mysqldump may embed raw BINARY(16) bytes, not just UTF-8 text.
        binary_id = bytes.fromhex('00f1a8e3b4e2e9c8dede000f10203040')
        binary_sql = b"CREATE TABLE `users` (`id` binary(16),`username` varchar(100),`password` varchar(500));\n" \
            + b"INSERT INTO `users` VALUES (_binary '" + binary_id + b"','person@example.invalid','sensitive');\n"
        self.src.write_bytes(binary_sql)
        worker = Anonymizer(KEY, {'allow_columns': []})
        worker.prepass(self.src)
        worker.transform(self.src, self.out)
        result = self.out.read_bytes()
        self.assertIn(binary_id, result)  # PK is preserved without byte conversion.
        self.assertIn(b'user-1@solyton.org', result)
        self.assertNotIn(b'person@example.invalid', result)
        self.assertNotIn(b'sensitive', result)

    def test_bank_and_user_profile_realistic_field_lengths(self):
        s = "CREATE TABLE `user_profiles` (`id` int,`first_name` varchar(50),`phone_number` varchar(15));\n" \
            + "INSERT INTO `user_profiles` VALUES (1,'Maria','+49 170 12345');\n"
        worker = self.worker(s)
        worker.transform(self.src, self.out)
        with self.out.open() as f:
            ins = parse_insert([i for i in iter_statements(f) if 'INSERT' in i][0])
        self.assertLessEqual(len(unquote(ins.rows[0][2])), 15)
        self.assertNotIn('Maria', self.out.read_text())

    def test_config_rule_redaction(self):
        sample = "CREATE TABLE `x` (`id` int,`extra_field` varchar(100));\nINSERT INTO `x` VALUES (1,'private');\n"
        w = self.worker(sample, {'rules': {'x.extra_field': 'redact'}})
        w.transform(self.src, self.out)
        self.assertIn('[REDACTED]', self.out.read_text())
        self.assertNotIn('private', self.out.read_text())

    def test_unknown_schema_audit(self):
        w = self.worker('CREATE TABLE `x` (`id` int,`custom` varchar(100));')
        self.assertIn('x.custom', w.audit()['unknown_columns'])


if __name__ == '__main__':
    unittest.main()
