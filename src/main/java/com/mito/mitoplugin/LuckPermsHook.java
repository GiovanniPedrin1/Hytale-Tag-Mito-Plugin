package com.mito.mitoplugin;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Hook opcional para LuckPerms (Hytale).
 * Não referencia classes do LuckPerms diretamente para não quebrar se não existir no runtime.
 */
public final class LuckPermsHook {
    private final Object playerAdapter; // PlayerAdapter<PlayerRef>

    private final Method getMetaDataMethod; // playerAdapter.getMetaData(PlayerRef)
    private final Method getPrefixMethod;   // CachedMetaData#getPrefix()

    private final Method getPermissionDataMethod;
    private final Method checkPermissionMethod;
    private final Method asBooleanMethod;

    private LuckPermsHook(Object playerAdapter,
                          Method getMetaDataMethod,
                          Method getPrefixMethod,
                          Method getPermissionDataMethod,
                          Method checkPermissionMethod,
                          Method asBooleanMethod) {
        this.playerAdapter = playerAdapter;
        this.getMetaDataMethod = getMetaDataMethod;
        this.getPrefixMethod = getPrefixMethod;
        this.getPermissionDataMethod = getPermissionDataMethod;
        this.checkPermissionMethod = checkPermissionMethod;
        this.asBooleanMethod = asBooleanMethod;
    }

    /** Tenta criar o hook. Retorna null se LuckPerms não estiver presente. */
    public static LuckPermsHook tryCreate() {
        try {
            // LuckPermsProvider.get()
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Method getMethod = providerClass.getMethod("get");

            Object api;
            try {
                api = getMethod.invoke(null);
            } catch (InvocationTargetException ite) {
                // Se for "NotLoadedException", só retorna null e tenta depois.
                // Para qualquer outra coisa, loga.
                Throwable cause = ite.getCause();
                if (cause != null && cause.getClass().getName().endsWith("NotLoadedException")) {
                    return null;
                }
                throw ite;
            }

            // api.getPlayerAdapter(PlayerRef.class)
            Method getPlayerAdapter = api.getClass().getMethod("getPlayerAdapter", Class.class);
            Object adapter = getPlayerAdapter.invoke(api, PlayerRef.class);

            // adapter.getMetaData(<T>)  (T vira Object no bytecode em alguns builds)
            Method getMetaData = findOneArgMethod(adapter.getClass(), "getMetaData");
            Method getPrefix = getMetaData.getReturnType().getMethod("getPrefix");

            // adapter.getPermissionData(<T>)
            Method getPermissionData = findOneArgMethod(adapter.getClass(), "getPermissionData");
            Method checkPermission = getPermissionData.getReturnType().getMethod("checkPermission", String.class);
            Method asBoolean = checkPermission.getReturnType().getMethod("asBoolean");

            return new LuckPermsHook(adapter, getMetaData, getPrefix, getPermissionData, checkPermission, asBoolean);

        } catch (Throwable t) {
            return null;
        }
    }

    /** Retorna o prefix do player, ou "" se não houver. */
    public String getPrefix(PlayerRef playerRef) {
        try {
            Object metaData = getMetaDataMethod.invoke(playerAdapter, playerRef);
            Object prefixObj = getPrefixMethod.invoke(metaData);
            if (prefixObj == null) return "";
            return String.valueOf(prefixObj);
        } catch (Throwable ignored) {
            return "";
        }
    }

    public boolean hasPermission(PlayerRef playerRef, String permission) {
        try {
            Object permissionData = getPermissionDataMethod.invoke(playerAdapter, playerRef);
            Object tristateResult = checkPermissionMethod.invoke(permissionData, permission);
            Object bool = asBooleanMethod.invoke(tristateResult);
            return (!(bool instanceof Boolean)) || !((Boolean) bool);
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static Method findOneArgMethod(Class<?> type, String name) throws NoSuchMethodException {
        for (Method m : type.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1) {
                m.setAccessible(true);
                return m;
            }
        }
        throw new NoSuchMethodException(type.getName() + "." + name + "(*)");
    }
}

