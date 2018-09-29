package win.somereason.utils.object_copier.entity;

/**
 * 集合(包括list和map)的复制规则
 */
public enum CollectionCopyRule {

    /**
     * 源对象覆盖目标对象.
     * 删除target里的所有元素,并用source的覆盖
     */
    OVER_WRITE(1),
    /**
     * 半合并.
     * 以source中的元素为主,但是如果target有相同的元素,那么会对相同的元素进行合并.
     *
     */
    JOIN(2),
    /**
     * 完全合并.
     * 源集合和目标集合里的元素都会保留,重复的会合并.
     */
    FULL_JOIN(3);

    private int numVal;

    CollectionCopyRule(int numVal) {
        this.numVal = numVal;
    }

    public int value() {
        return numVal;
    }
}
