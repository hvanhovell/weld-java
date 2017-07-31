package weld

/**
 * Wrapper for a weld native pointer.
 */
case class WeldPointerWrapper(address: Long, dataType: PointerType)
