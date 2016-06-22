package org.bitcoins.core.protocol.script

import org.scalacheck.{Prop, Properties}

/**
  * Created by chris on 6/22/16.
  */
class P2PKHScriptPubKeySpec extends Properties("P2PKHScriptPubKeySpec") {

  property("Serialization symmetry") =
    Prop.forAll(ScriptGenerators.p2pkhScriptPubKey) { p2pkhScriptPubKey =>
      P2PKHScriptPubKey(p2pkhScriptPubKey.hex) == p2pkhScriptPubKey

    }
}
