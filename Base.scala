object Base {
  def toDecimal (li: List[Int], base: Int) : BigInt = li match {                       
    case Nil => BigInt (0)                                                             
    case x :: xs => BigInt (x % base) + (BigInt (base) * toDecimal (xs, base)) }  

  def fromDecimal (dec: BigInt, base: Int) : List[Int] =
    if (dec==0L) Nil else (dec % base).toInt :: fromDecimal (dec/base, base)

  def x2y (value: List[Int], from: Int, to: Int) =
    fromDecimal (toDecimal (value.reverse, from), to).reverse
  def test (li: List[Int], from: Int, to: Int, s: String) = {
    val erg= "" + x2y (li, from, to)
    if (! erg.equals (s))
      println ("2dec: " + toDecimal (li, from) + "\n\terg: " + erg + "\n\texp: " + s)
  }   

  def main(args: Array[String]){
    test (List (1, 2, 3, 4), 16, 16, "List(1, 2, 3, 4)")
    test (List (1, 0), 10, 100, "List(10)")
    test (List (41, 15, 156, 123, 254, 156, 141, 2, 24), 256, 16, "List(2, 9, 0, 15, 9, 12, 7, 11, 15, 14, 9, 12, 8, 13, 0, 2, 1, 8)") 
    test (List (1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), 2, 10, "List(1, 2, 3, 7, 9, 4, 0, 0, 3, 9, 2, 8, 5, 3, 8, 0, 2, 7, 4, 8, 9, 9, 1, 2, 4, 2, 2, 3)") 
    test (List (41, 42, 43), 256, 36, "List(1, 21, 29, 22, 3)")
  }
}