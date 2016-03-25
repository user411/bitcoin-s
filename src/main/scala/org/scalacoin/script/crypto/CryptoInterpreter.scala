package org.scalacoin.script.crypto

import org.scalacoin.crypto.{DERSignatureUtil, TransactionSignatureChecker, ECFactory, TransactionSignatureSerializer}
import org.scalacoin.protocol.script._
import org.scalacoin.protocol.transaction.Transaction
import org.scalacoin.script.control.{ControlOperationsInterpreter, OP_VERIFY}
import org.scalacoin.script.flag.ScriptVerifyDerSig
import org.scalacoin.script.{ScriptProgramFactory, ScriptProgramImpl, ScriptProgram}
import org.scalacoin.script.constant._
import org.scalacoin.util.{BitcoinSLogger, BitcoinSUtil, CryptoUtil}
import org.slf4j.LoggerFactory


/**
 * Created by chris on 1/6/16.
 */
trait CryptoInterpreter extends ControlOperationsInterpreter with BitcoinSLogger {

  /**
   * The input is hashed twice: first with SHA-256 and then with RIPEMD-160.
   * @param program
   * @return
   */
  def opHash160(program : ScriptProgram) : ScriptProgram = {
    require(program.stack.headOption.isDefined, "The top of the stack must be defined for OP_HASH160")
    require(program.script.headOption.isDefined && program.script.head == OP_HASH160, "Script operation must be OP_HASH160")
    val stackTop = program.stack.head
    val hash = ScriptConstantImpl(BitcoinSUtil.encodeHex(CryptoUtil.sha256Hash160(stackTop.bytes)))
    ScriptProgramFactory.factory(program, hash :: program.stack, program.script.tail)
  }


  /**
   * The input is hashed using RIPEMD-160.
   * @param program
   * @return
   */
  def opRipeMd160(program : ScriptProgram) : ScriptProgram = {
    require(program.stack.headOption.isDefined, "The top of the stack must be defined for OP_RIPEMD160")
    require(program.script.headOption.isDefined && program.script.head == OP_RIPEMD160, "Script operation must be OP_RIPEMD160")
    val stackTop = program.stack.head
    val hash = CryptoUtil.ripeMd160(stackTop.bytes)
    val newStackTop = ScriptConstantImpl(BitcoinSUtil.encodeHex(hash))
    ScriptProgramFactory.factory(program,newStackTop :: program.stack.tail, program.script.tail)
  }

  /**
   * The input is hashed using SHA-256.
   * @param program
   * @return
   */
  def opSha256(program : ScriptProgram) : ScriptProgram = {
    require(program.stack.headOption.isDefined, "The top of the stack must be defined for OP_SHA256")
    require(program.script.headOption.isDefined && program.script.head == OP_SHA256, "Script operation must be OP_SHA256")
    val stackTop = program.stack.head
    val hash = CryptoUtil.sha256(stackTop.bytes)
    val newStackTop = ScriptConstantImpl(BitcoinSUtil.encodeHex(hash))
    ScriptProgramFactory.factory(program, newStackTop :: program.stack.tail, program.script.tail)
  }

  /**
   * The input is hashed two times with SHA-256.
   * @param program
   * @return
   */
  def opHash256(program : ScriptProgram) : ScriptProgram = {
    require(program.stack.headOption.isDefined, "The top of the stack must be defined for OP_HASH256")
    require(program.script.headOption.isDefined && program.script.head == OP_HASH256, "Script operation must be OP_HASH256")
    val stackTop = program.stack.head
    val hash = CryptoUtil.doubleSHA256(stackTop.bytes)
    val newStackTop = ScriptConstantImpl(BitcoinSUtil.encodeHex(hash))
    ScriptProgramFactory.factory(program, newStackTop :: program.stack.tail, program.script.tail)
  }


  /**
   * The entire transaction's outputs, inputs, and script (from the most
   * recently-executed OP_CODESEPARATOR to the end) are hashed.
   * The signature used by OP_CHECKSIG must be a valid signature for this hash and public key.
   * If it is, 1 is returned, 0 otherwise.
   * https://github.com/bitcoin/bitcoin/blob/master/src/script/interpreter.cpp#L818
   * @param program
   * @return
   */
  def opCheckSig(program : ScriptProgram) : ScriptProgram = {
    require(program.script.headOption.isDefined && program.script.head == OP_CHECKSIG, "Script top must be OP_CHECKSIG")
    require(program.stack.size > 1, "Stack must have at least 2 items on it for OP_CHECKSIG")

    val (pubKey,signature) = program.scriptSignature match {
      case _ : MultiSignatureScriptSignature | _ : P2SHScriptSignature | _ : P2PKHScriptSignature | _ : P2PKScriptSignature | _ : NonStandardScriptSignature =>
        val pubKey = ECFactory.publicKey(program.stack.head.bytes)
        val signature = ECFactory.digitalSignature(program.stack.tail.head.bytes)
        (pubKey,signature)
      case EmptyScriptSignature =>
        throw new RuntimeException("We do not know how to evaluate a nonstandard or EmptyScriptSignature for an OP_CHECKSIG operation")
    }


    if (program.flags.contains(ScriptVerifyDerSig) && !DERSignatureUtil.isStrictDEREncoding(signature)) {
      //this means all of the signatures must encoded according to BIP66 strict dersig
      //https://github.com/bitcoin/bips/blob/master/bip-0066.mediawiki
      //script verification fails since the sig is not strictly der encoded
      logger.warn("Since the ScriptVerifyDerSig flag is set the signature being checked must be a strict dersig signature as per BIP 66\n" +
        "Sig: " + signature.hex)
      ScriptProgramFactory.factory(program,false)
    } else {
      logger.debug("Old program isValid: " + program.isValid)
      val restOfStack = program.stack.tail.tail
      val hashType = (signature.bytes.size == 0) match {
        case true => SIGHASH_ALL
        case false => HashTypeFactory.fromByte(BitcoinSUtil.decodeHex(signature.hex).last).get
      }

      val hashForSig = TransactionSignatureSerializer.hashForSignature(program.transaction,
        program.inputIndex,program.scriptPubKey,hashType)
      logger.debug("Hash for sig inside of opChecksig: " + BitcoinSUtil.encodeHex(hashForSig))
      val isValid = pubKey.verify(hashForSig, signature)
      logger.debug("signature verification isValid: " + isValid)
      if (isValid) ScriptProgramFactory.factory(program, ScriptTrue :: restOfStack,program.script.tail)
      else ScriptProgramFactory.factory(program, ScriptFalse :: restOfStack,program.script.tail)

    }


  }


  /**
   * The input is hashed using SHA-1.
   * @param program
   * @return
   */
  def opSha1(program : ScriptProgram) : ScriptProgram = {
    require(program.script.headOption.isDefined && program.script.head == OP_SHA1, "Script top must be OP_SHA1")
    require(program.stack.headOption.isDefined, "We must have an element on the stack for OP_SHA1")

    val constant = program.stack.head
    val hash = ScriptConstantImpl(BitcoinSUtil.encodeHex(CryptoUtil.sha1(constant.bytes)))
    ScriptProgramFactory.factory(program, hash :: program.stack.tail, program.script.tail)
  }

  /**
   * All of the signature checking words will only match signatures to the data
   * after the most recently-executed OP_CODESEPARATOR.
   * @param program
   * @return
   */
  def opCodeSeparator(program : ScriptProgram) : ScriptProgram = {
    require(program.script.headOption.isDefined && program.script.head == OP_CODESEPARATOR, "Script top must be OP_CODESEPARATOR")

    //get the index of this OP_CODESEPARATOR
    val codeSeparatorIndex = program.fullScript.size - program.script.size
    ScriptProgramFactory.factory(program,program.script.tail, ScriptProgramFactory.Script, codeSeparatorIndex)
  }


  /**
   * Compares the first signature against each public key until it finds an ECDSA match.
   * Starting with the subsequent public key, it compares the second signature against each remaining
   * public key until it finds an ECDSA match. The process is repeated until all signatures have been
   * checked or not enough public keys remain to produce a successful result.
   * All signatures need to match a public key.
   * Because public keys are not checked again if they fail any signature comparison,
   * signatures must be placed in the scriptSig using the same order as their corresponding public keys
   * were placed in the scriptPubKey or redeemScript. If all signatures are valid, 1 is returned, 0 otherwise.
   * Due to a bug, one extra unused value is removed from the stack.
   * @param program
   * @return
   */
  def opCheckMultiSig(program : ScriptProgram) : ScriptProgram = {
    require(program.script.headOption.isDefined && program.script.head == OP_CHECKMULTISIG, "Script top must be OP_CHECKMULTISIG")
    require(program.stack.size > 2, "Stack must contain at least 3 items for OP_CHECKMULTISIG")

    //TODO: Check if strict dersig flag
    //head should be n for m/n
    val nPossibleSignatures : Int  = program.stack.head match {
      case s : ScriptNumber => s.num.toInt
      case _ => throw new RuntimeException("n must be a script number for OP_CHECKMULTISIG")
    }

    logger.debug("nPossibleSignatures: " + nPossibleSignatures)

    val pubKeys : Seq[ScriptToken] = program.stack.tail.slice(0,nPossibleSignatures)
    val stackWithoutPubKeys = program.stack.tail.slice(nPossibleSignatures,program.stack.tail.size)

    val mRequiredSignatures : Int = stackWithoutPubKeys.head match {
      case s: ScriptNumber => s.num.toInt
      case _ => throw new RuntimeException("m must be a script number for OP_CHECKMULTISIG")
    }

    logger.debug("mRequiredSignatures: " + mRequiredSignatures )

    //+1 is for bug in OP_CHECKMULTSIG that requires an extra OP to be pushed onto the stack
    val stackWithoutPubKeysAndSignatures = stackWithoutPubKeys.tail.slice(mRequiredSignatures+1, stackWithoutPubKeys.tail.size)

    val restOfStack = stackWithoutPubKeysAndSignatures

    val result = TransactionSignatureChecker.checkSignature(program.transaction,program.inputIndex,program.scriptPubKey)
    //if there are zero signatures required for the m/n signature
    //the transaction is valid by default
    if (result) ScriptProgramFactory.factory(program, ScriptTrue :: restOfStack, program.script.tail,true)
    else ScriptProgramFactory.factory(program, ScriptFalse :: restOfStack, program.script.tail,false)
  }


  /**
   * Runs OP_CHECKMULTISIG with an OP_VERIFY afterwards
   * @param program
   * @return
   */
  def opCheckMultiSigVerify(program : ScriptProgram) : ScriptProgram = {
    require(program.script.headOption.isDefined && program.script.head == OP_CHECKMULTISIGVERIFY, "Script top must be OP_CHECKMULTISIGVERIFY")
    require(program.stack.size > 2, "Stack must contain at least 3 items for OP_CHECKMULTISIGVERIFY")
    val newScript = OP_CHECKMULTISIG :: OP_VERIFY :: program.script.tail
    val newProgram = ScriptProgramFactory.factory(program,newScript, ScriptProgramFactory.Script)
    val programFromOpCheckMultiSig = opCheckMultiSig(newProgram)
    val programFromOpVerify = opVerify(programFromOpCheckMultiSig)
    programFromOpVerify
  }

}
