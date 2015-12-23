package utils

/**
 * 文字列操作関連のユーティリティ。
 */
object TextUtil {
  /**
   * 全角スペースもトリムします。
   * @param value トリム対象の文字列
   * @return トリム後の文字列
   */
  def trim(value: String): String = {
    val valueLen = value.length()
    var len = valueLen

    if (value == null || valueLen == 0) {
      value
    } else {
      var st = 0

      val valChars: Array[Char] = value.toCharArray

      while ((st < len) && ((valChars(st) <= ' ') || (valChars(st) == '　'))) {
        st = st + 1
      }

      while ((st < len) && ((valChars(len - 1) <= ' ') || (valChars(len - 1) == '　'))) {
        len = len - 1
      }

      if (st > 0 || len < valueLen) {
        value.substring(st, len)
      } else {
        value
      }
    }
  }

  def concatHex(buf: Seq[Byte]): String = buf.map("%02x" format _).foldLeft("")(_ + _)

  private val u = BigDecimal(1024)
  private val K = u
  private val M = K * u
  private val G = M * u
  private val T = G * u
  private val P = T * u
  private val E = P * u
  private val Z = E * u
  private val Y = Z * u
  private val prefix = Seq((Y, "Y"), (Z, "Z"), (E, "E"), (P, "P"), (T, "T"), (G, "G"), (M, "M"), (K, "K"))

  /**
   * 指定された数と等しくなるような数とSI接頭辞の文字列を返す。
   * ただし、|n| < 1024の場合はSI接頭辞の代わりに空文字列を返す。
   * SI接頭辞は、数の絶対値がが１以上で最も小さくなるようなものが選択される。
   * @return (SI接頭辞でnを表した時の数, |0| < 1024の場合空文字列、それ以外の場合K以上のSI接頭辞)
   */
  def toSiPrefixNotation(n: BigDecimal): (BigDecimal, String) = {
    prefix.find(_._1 <= n.abs) match {
      case Some((prefixValue, prefixText)) =>
        (n / prefixValue, prefixText)
      case _ =>
        (n, "")
    }
  }
}

