package com.yahoo.athenz.zts.store;

import com.yahoo.athenz.common.server.store.ChangeLogStore;

public class CustomizedDataStore extends DataStore {
    public CustomizedDataStore(ChangeLogStore clogStore, CloudStore cloudStore) {
        super(clogStore, cloudStore);
    }
}
