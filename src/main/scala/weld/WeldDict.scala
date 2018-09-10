package weld

/**
 * Wrapper for a weld dictionary.
 */
case class WeldDict(address: Long, objectType: DictType) {
  override def toString(): String = {
    s"WeldDict[$objectType]($address)"
  }
}
