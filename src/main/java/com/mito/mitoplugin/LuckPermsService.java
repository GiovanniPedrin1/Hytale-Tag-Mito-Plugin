package com.mito.mitoplugin;

public final class LuckPermsService {
    private volatile LuckPermsHook hook;

    public LuckPermsHook get() {
        if (hook != null) return hook;
        hook = LuckPermsHook.tryCreate(); // pode voltar null
        return hook;
    }
}
