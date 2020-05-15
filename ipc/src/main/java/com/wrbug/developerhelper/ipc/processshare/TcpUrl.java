package com.wrbug.developerhelper.ipc.processshare;

public class TcpUrl {
    public static class AppXposedProcessDataUrl {
        public static final String SET_APP_XPOSED_STATUS_LIST = "AppXposedProcessData/setAppXposedStatusList";
        public static final String GET_APP_XPOSED_STATUS_LIST = "AppXposedProcessData/getAppXposedStatusList";

    }

    public static class DumpDexListProcessDataUrl {
        public static final String SET_DATA = "DumpDexListProcessData/setData";
        public static final String GET_DATA = "DumpDexListProcessData/getData";
    }

    public static class GlobalConfigProcessDataUrl {
        public static final String IS_XPOSED_OPEN = "GlobalConfigProcessData/isXposedOpen";
        public static final String SET_XPOSED_OPEN = "GlobalConfigProcessData/setXposedOpen";
    }
}
