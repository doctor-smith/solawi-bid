package org.solyton.solawi.bid.module.user.data.reader

import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.language.subComp
import org.evoleq.math.Reader

val inputs: Reader<Lang.Block, Lang.Block> = subComp("inputs")

val username: Reader<Lang.Block, Lang.Block> = subComp("username")

val password: Reader<Lang.Block, Lang.Block> = subComp("password")

val oldPassword: Reader<Lang.Block, Lang.Block> = subComp("oldPassword")

val newPassword: Reader<Lang.Block, Lang.Block> = subComp("newPassword")

val repeatPassword: Reader<Lang.Block, Lang.Block> = subComp("repeatPassword")

val changePassword: Reader<Lang.Block, Lang.Block> = subComp("changePassword")

val errors: Reader<Lang.Block, Lang.Block> = subComp("errors")

val wrongPassword: Reader<Lang.Block, String> = Reader{block -> block["wrongPassword"]}

val requirementsViolated: Reader<Lang.Block, String> = Reader{block -> block["requirementsViolated"]}

val repeatedPasswordMismatch: Reader<Lang.Block, String> = Reader{block -> block["repeatedPasswordMismatch"]}

val newPasswordEqualsOld: Reader<Lang.Block, String> = Reader{block -> block["newPasswordEqualsOld"]}


val personalData: Reader<Lang.Block, Lang.Block> = subComp("personalData")

val properties: Reader<Lang.Block, Lang.Block> = subComp("properties")

val value: Reader<Lang.Block, String> = Reader { block -> block["value"] }
