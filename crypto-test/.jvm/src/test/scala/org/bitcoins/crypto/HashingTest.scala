package org.bitcoins.crypto

import scodec.bits._

class HashingTest extends BitcoinSCryptoTest {

  private lazy val lines = Vector(
    // rnd,sha1(rnd),sha256(rnd),ripeMd160(rnd),sha256Hash160(rnd),hmac512(rnd,sha256),sha3_256(rnd),hmac256(rnd,sha256)
    "41dc440cf30c42cb17b935d3d8451b0c1e2a8ad9a529637228df5ca31a10c0a2,b600f58e73defdbb68650078f39760ba1bcfb391,4eb2b43a30cca0aebdefec2f6a30abb56a1f6d9b0462d252184dc5209c62d5d0,ddb6092b1103067c609b35bec4177abf5dfc9f5c,304d0d0762ec66b44764a11b0d02e234b1d75721,36d248dc8a9825ac48c32fe4ebf0b4dd8ee450c226f05dc8b8cfac62dbcc11a89bc00ab3942bc5ca74a7f189bff04fe9541b749a7670e9dcf36e3bdb37cd6e01,870d8f928cc97f37b6e0845f9f8bde004f24a12ee36cc5c5408a8dc67eb9d051,2209d8041b4a0d2a37df64c4f55aec1f1b22244c8d7e16c133954ce1502d8cfd",
    "a361ef0fdc98778bd5c6f6c50a6300f560a88644029a623294091d2b3d0e22d7,7cb911a6852779261cc151410691bd408b23ba1d,d63c99b1abffc915ba9e35fdc5763304839d981dc25c106bdd90a8bdc4eb6f13,6003b2e364b11f2737c7fb4cb190fdd55783f76b,a4f38c39eab8afe5f2e63d1a89d0c365c96cc7a7,87c59c1c9535043a3c3ecdec25776613f198ae4707675c129a1ed8d217aac20f7d07ce4c84e6cfa833ee7d3e52b3673730b900e6ca7075185d1ecce473110447,48d322b5b8c781bd027686f503682e2efc37f621bb5fef14ddfad9329f97bbbb,b28db904f810688faa6e66de09f82e43f89257a53547a5693e62da62dbf555ec",
    "35659ae4880ae74deda82a5d3b77ba8fc4adff84e1c46fa67955338bc7346977,e379fb92c9500d410818bd95796a87f662f1fd76,904a2128262e055f68896150e1b1da93849bda2054e5e6f6f8db11214a1b6855,a63e15d313afef12927f38924a97927ac3e61b8b,0c2109858080cad92ec93e883998b6048b671112,7bdd176479a379bb9447e5f7646cd0be210977b593d6a71fbdbe545a282b4fb9e22b6ec5d0afa62045fe96074b91544755f9c28ee0af368ae34165e5487d65d2,d34318f7c0ae01caaebca717a6388cae96e3f8c506acd3f152d5619893d4defb,7ef68c0e93c83ddc7c52894951b15a25b5db67b7d63e219a371b232fd4a5d612",
    "e3c54af8504a7f0d69f8f9c9b29c5d15fc861821547f285bb70d2850ce08efe9,a93c7e60aa08e08919c9085641ff1a7cd3400613,1325fb6c0029b128b0fdf37d014e3739aea56fc670756e335a2ce34fe08c2574,d5409d92add4c4f7fad2ae8eea100e19e2f1e66f,90dcb2f658418089a0d41abb7f012f2531f860a0,911a7b19f7f0f145921002ed3830d46045041e786cbea13c7f77066ed8a03d56ddaa2db987358f6a4c713fddd07570647fd80ea072da309a60758c305137d4a8,1b9a19ea9447d58fa980091d04e20b9f9b4999b54b415cc522089bfe664a6319,71014e882f5693a2af5cf730cb5df40ae436ace93b5519d21249d401e80a6f5c",
    "8283f8f51b5045ef414e462ebb5c9573950ec0162578df73d0f5490febac5a97,40ebb9af58651c51135641278884217f23f14993,540ed853ed97664d7ea82e663d2480076d1b81088a5ef405465871c8df3d1b9f,4c69787f663f188fbcee4b0d9a737083e018ac08,137b3fb7855dc321348e03c458ecc18daedf0f95,5172f81bbabd00b9c81654826dba9e527b5a1c553b9c2c92b7b59b420d5b8064c2fd3f9e8107ec3a07298f712de10a126d348d2bb6dac21aa397a665a1ee787d,eb4080b54011e5cd852d20b933412a1615f1d6fce96bb00af4d47b70b95f6b64,0da1a118de1dcd82e86d409d1436cf964d5f317be432f55b5aa8258d7420f59c",
    "6286477a8da8d7b6f2fc9080d5e7a4091b1d506ee2dc4f81740b0f54f438537d,aeded4ba719eb6fed787d34b8064402082db7a4d,3df54218915767977bdf2bcde8b45a711caa460c3baa2448f91bc13731e6ea3d,946d3624e583fef1b46bdb8c24155d62083c0b47,839cff76a9b045f49a0b7c44f34d8561dc442870,280245f199d155794032fb4ea90f22c38de688bfb4e43c66507994610d01676b0c05eb1d531fe4cb628fee5ff00b863feec037088e128c0d4e875816c9a4b6bd,1d50401d65343f79ef63368043a0beba4171b86ef1991ec2b0be54b0f2e3f70a,5538ffa08847a156d9fdeac722c382ab0805fec113fa028e0c3abcc952728859",
    "47a8bb983cfc0387769dd6b80b8674c0531545c31cef0feff5c1113326003510,025c7f81895c1cd8e92c79a61a11b4b900eaf475,da19d198af8bcb060b07a8825f6898ac821b002b0e5da574cab2753bee430f8a,22292f488c729250acf0d2f7147fd6b2e6a808db,da91c4f8751060450782a9b023f6f5f4f2a0ee9f,aaebe082b58dce13087555d9ada0ec6c965acd39a014d47ad9f43700a8f36e466c5117165b4d585cc5bc5b295d9bbd6dcc8d27cce4a1c563ed1cfe1353186b79,73057ed727c4b004c3f3af867c667bff8b6671271a689c8ca99dd82f098e341b,5c9d08c9a8835c69e25d1543d4df665f815a1ecca5b4006b9040b5aed057e286",
    "8a329193ab14c37b397d749d1c254d9c5a962248cd2ad48460fb7cf659e89f52,ff4090d399bbb8e3220bc57ed13c6a0c44c79ea5,20460d4766c94a72ddef09c83f5eea9545af7826a446539c1a07291f6cdc5af4,eaf846e713cf63704cf1d80fd415265f4b5d6586,d45c9dc61b959c3548fe5c2de3781714d1dcf9f7,e5e62e56726480551da0b88de1657676979f3f5e6a028b3a0c91a186495060ebf5e4cab2c71675f36bd4b86c6ab8a1ee850eaffd93c28828504ae023783f6005,fcf3adea6f02d4f2bc2ef3f0d2be9f190524c80d65a1d97f1f358e5048938229,27f7266322662491ba01f04c461276d935949414925e72980bce5f92cdf9fb97",
    "96c37689f01bd39b6f163fbf4b1025653ea196f0abdb8a5dcd8cbc76c78f8cf5,f7edf6a546b5c7023ae146ac1ef58256f0578b73,34957be30df1b91070c9b39e086b31906e295574d315eaf2e91c6b45a292bb84,84795b00f8405a6ca0fab024991f34187a1be0c1,2367689553879bbcea1ba7608eb1905fb8230565,98bc48d3da9fcb8723b5e37ae30a8aa54a411eede7a9e5220c9ae849cbd591577991922b8995ef34704618b23f1961770c9f31ebe346ca6089da391c96c6a11d,2c292189440176ae193a5326e82b67fe058603a5f12ab3382e69e2eaaf457e4f,ee1206aeb469c391575f6d5478b4ec4931753cca35f3783d092129005065c526",
    "7eff84aeca24260baa06b61394bdec8b760a0c6558dd1748d91d6f492e0779b7,1c182efc13f2468d880372f4809d5a5e7011dfbf,13d1c9c1d3ac10cc78c43a2f0724a6a2e06adf5844a4ab45ff4779af1f15029d,8ce4f2abb9775f7e3cea0fa75238ab75cf6908e3,d3e2938a69fdce888260003f6f1702e9d3392ab0,4569f2282b016fee5b583390e709dd285eb09b7510bbe092f37e5fef581a78b42729eb9f46088d1d52a00f2f57d1ffd48ab64b5c0dfc38d01bf2c793c977fcfc,a1804b2d4eabf9a4ac77531f5f12d1b12fa5d7042d7e526c6c59ea7d4259d338,b41dcbf88fec506a45d8fbd1c36caf7224117c6973aa23b55ef58e6a59d11f8f",
    "c7acfcf801519b59c4750b06d6d55b1915f3a0317859685dd2ae337a6065d4bb,384c59c208e92c46e4fb8bcea9e286181ee4f956,4853fbb9eafeb0bfe2d717ad34973ea979413874e97d92d7a85c16a8fb61626e,e45f80b6cd0533e8b6f9a974b17c20244812e0fe,d531970c5f1ca7d0772513b32e8b75fa837c27fd,fc7946c37e09c86ce48e704f162e96c06cd227df7813dcc910fb56793a8cd5b0f205207292f6c7d34aeddeae80d3a6243409f2d4bd11e0f4eba5e6711df77586,cf385747877b360b3582331aa1473039946c57c59d7969988b103c26cd43c949,b10bd5085b7b6ee5a0f98009de28abdbe4e590c0c7224f6aab11ef4177196fda",
    "b53e7fb86d09133c5d1bfccae6a16652d26376784ec97995c9173f8d4d9151bc,69a8d04118b7f4e5c49a877ea2a1e6d9bdcc6dc5,33cb2e45fe278f822c9f8f0b0f6ca7c0aa84e588237a70e0870607756bdb18e0,f17ef3913819472407b45b9bf2bf454b4185bfe7,75cd9d71a818ccfc234c80cd89b6b52b898530bc,008892ec410fcc7197049a9967738edff5abeef1e29eb03a2b3f1939433f16ce284a9d3be887b3b1500161a38a617e8e467b4564bb6d030f617fc996d3a26dd2,b8b865556a937c3c0dbed23abe2d40ed424b5fb53d22ed83db9eec00dc15e4cf,88d14a6316082bcfe1a57bcfa46b1378baf60ea179063b2b78ec22f205a63a52",
    "7125adb44be6dc3bea98c348fc546b92371e99b4294d753c3a2c408447057a48,06fed168e3f0a8928d56c847364caeaf63c11f34,721e34ca27da076c1e18b51ffbf1f2921a431f55e83f85c4cbe679d5e081c120,023445e83064df92dcd60478cd68ea45f032d5c0,d0e99a8e4a7ba80bcc861b6dc81c29547dfd7dae,a485d3f5f22d70dae68176b76df8f8173adae876dcae8e93c5156277204a507484a50cc25b40e967ed75c4eaf29cda8e97ab8b05f72fa30a28e11ed9a30c6f7a,4e788e256ab33c5143425544bfa179e4a0a529142670dd45cda42f26a3b8d4f0,f3c53a8264f688f61855909b2d31afdca8ebe79446afe96e0e35441df08d353c",
    "34ab9bc63b31a019e59af803cd9365500ed01e36e5d8e166d7fa22a3dd06a4b2,495804100e19df440a1026e016c3a55800064bed,203f617d00fada67c5d5f48736793d79d3b75633e8977d531a035b51df696853,4278ed1405c4d61d35bf2dbd8b0ad17cf8eb2de8,23656338b7894db5ca7c5804e91a128cf0d17acf,4ab854a2ce4f7a2ac71fb0b84b88f960cc0fd7467b9a6e298243b0117e8d38525876608fa403d43bceb2aee43abebac670db6e74a2f346cb1a254aa942cc1fbd,666855a84f4be9a469296f20dd660126490ecf2d7bacda3f9e2a0dd11ca20636,34d4f8e6a59b38433083f82511d6ba2583b3d9425561770dc50f58ce8a275a0f",
    "93dc29da56fac0a611c8c5014bc61a6c3861ae9def571818664c7e44b25a954d,ceb1b9394c434c5a1648112477c5f674ed4430ca,f3a1d0c012317a4802aa52ae17c03e6bb8f769d35c909af8c0dbab16fe238d79,31c690c105e778b5941492cdffa5a817adc14202,ac6975f571f6e961c3db6ac29cfc7e975b2aaa09,f2f758a0e28f40d2b13a590d57875295668f12725a1536b513b19f59b65f88f885229ef424f8b226ee5b53749097ca70aa8b6dbb3650779074775239c652b70c,f091844b69ab5e3075255831b61ef62712adecbb9403c4b2b4f2719786bc4405,e31432e826bb45e09348ea1426dc96840ec4011b0e22efb81d3a65b357a7e61d",
    "f6372c5a3c6957b655d56167a4e723fc0eefdd3abf1857b2c7d797a0be0e4b0e,960b962cea70b57c7edc4f137261f94829957a3b,70eeaa3dc29f434da194e23bb5eb1001e9012bab15bec0fb9532c63f022c19a8,80f5a9119a99ef6bc04a1944f256fc8176216ad2,d535e38f334aa06538ea644849b80972a7fe6c6f,ac08b0031b5026157997b5c4a5f60df1c8afd39c5d7691033c72d65e4fa06312b48dd0cf46dd350188563c957ddabdb4b06ed2e9b4be2babcf640825358b555e,bf166ee74cf3c2dfcc88dd9c6d548438b9b76fa03df6589990997f44fa4b6f13,414b0553e95a2357b980e000777098f8fbc8f373d00814f0ec33b2d1a4e2c2ae"
  )

  private lazy val hashes = lines.map { line =>
    val arr = line.split(",")
    val data = ByteVector.fromValidHex(arr(0))
    val sha1 = Sha1Digest.fromHex(arr(1))
    val sha256 = Sha256Digest.fromHex(arr(2))
    val ripeMd160 = RipeMd160Digest.fromHex(arr(3))
    val sha256Hash160 = Sha256Hash160Digest.fromHex(arr(4))
    val hmac512 = ByteVector.fromValidHex(arr(5))
    val sha3_256 = Sha3_256Digest.fromHex(arr(6))
    val hmac256 = ByteVector.fromValidHex(arr(7))
    (data, sha1, sha256, ripeMd160, sha256Hash160, hmac512, sha3_256, hmac256)
  }

  it must "compute sha1" in {
    val expected = hashes.map(_._2)
    val actual = hashes.map(_._1).map(BouncycastleCryptoRuntime.sha1)
    assert(actual == expected)
  }

  it must "compute sha256" in {
    val expected = hashes.map(_._3)
    val actual = hashes.map(_._1).map(BouncycastleCryptoRuntime.sha256)
    assert(actual == expected)
  }

  it must "compute ripemd160" in {
    val expected = hashes.map(_._4)
    val actual = hashes.map(_._1).map(BouncycastleCryptoRuntime.ripeMd160)
    assert(actual == expected)
  }

  it must "compute sha256hash160" in {
    val expected = hashes.map(_._5)
    val actual = hashes.map(_._1).map(BouncycastleCryptoRuntime.sha256Hash160)
    assert(actual == expected)
  }

  it must "compute hmac512" in {
    val expected = hashes.map(_._6)
    val actual =
      hashes
        .map(x => (x._1, x._3.bytes))
        .map(y => BouncycastleCryptoRuntime.hmac512(y._1, y._2))
    assert(actual == expected)
  }

  it must "compute sha3-256" in {
    val expected = hashes.map(_._7)
    val actual = hashes.map(_._1).map(BouncycastleCryptoRuntime.sha3_256)
    assert(actual == expected)
  }

  it must "compute hmac256" in {
    val expected = hashes.map(_._8)
    val actual =
      hashes
        .map(x => (x._1, x._3.bytes))
        .map(y => BouncycastleCryptoRuntime.hmac256(y._1, y._2))
    assert(actual == expected)
  }

  // From https://github.com/dgarage/NDLC/blob/d816c0c517611b336f09ceaa43d400ecb5ab909b/NDLC.Tests/Data/normalization_tests.json
  it must "normalize and serialize strings correctly" in {
    val singletons = Vector("\u00c5", "\u212b", "\u0041\u030a")
    assert(
      singletons
        .map(BouncycastleCryptoRuntime.normalize)
        .forall(_ == "\u00c5")
    )

    val canonicalComposites = Vector("\u00f4", "\u006f\u0302")
    assert(
      canonicalComposites
        .map(BouncycastleCryptoRuntime.normalize)
        .forall(_ == "\u00f4")
    )

    val multipleCombiningMarks = Vector("\u1e69", "\u0073\u0323\u0307")
    assert(
      multipleCombiningMarks
        .map(BouncycastleCryptoRuntime.normalize)
        .forall(_ == "\u1e69")
    )
  }

}
