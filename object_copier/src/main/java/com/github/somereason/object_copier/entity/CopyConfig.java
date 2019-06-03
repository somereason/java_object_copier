package com.github.somereason.object_copier.entity;


public class CopyConfig {

    /**
     * 那么当source中某个字段是null的时候,如果copyNull==true,则会把dest的对应字段设置为null,如果copyNull==false,则dest的对应字段保留原值
     */
    protected Boolean copyNull;
    protected CollectionCopyRule collectionCopyRule;

    public void setCollectionCopyRule(CollectionCopyRule collectionCopyRule) {
        this.collectionCopyRule = collectionCopyRule;
    }

    public void setCopyNull(Boolean copyNull) {
        this.copyNull = copyNull;
    }

    public Boolean getCopyNull() {
        return copyNull;
    }

    public CollectionCopyRule getCollectionCopyRule() {
        return collectionCopyRule;
    }
}
