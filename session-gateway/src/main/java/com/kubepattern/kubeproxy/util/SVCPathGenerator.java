package com.kubepattern.kubeproxy.util;

public class SVCPathGenerator {
    public static String generatePath(String userName, String wsName, String appName) {
        if (wsName == null || wsName.isEmpty()) {
            if (appName == null || appName.isEmpty()) {
                return "/" + userName;
            } else {
                return "/" + userName + "/" + appName;
            }
        } else {
            if (appName == null || appName.isEmpty()) {
                return "/" + userName + "/" + wsName;
            } else {
                return "/" + userName + "/" + wsName + "/" + appName;
            }
        }
    }

    public static String generateName(String userName, String wsName, String appName) {
        if (wsName == null || wsName.isEmpty()) {
            if (appName == null || appName.isEmpty()) {
                return userName;
            } else {
                return userName + "-" + appName;
            }
        } else {
            if (appName == null || appName.isEmpty()) {
                return userName + "-" + wsName;
            } else {
                return userName + "-" + wsName + "-" + appName;
            }
        }
    }
}
