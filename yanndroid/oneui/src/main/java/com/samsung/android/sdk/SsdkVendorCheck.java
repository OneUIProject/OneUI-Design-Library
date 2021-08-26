package com.samsung.android.sdk;

import android.os.Build;

public class SsdkVendorCheck {
    private static String strBrand = Build.BRAND;
    private static String strManufacturer = Build.MANUFACTURER;

    private SsdkVendorCheck() {
    }

    public static boolean isSamsungDevice() {
        if (strBrand == null || strManufacturer == null) {
            return false;
        }
        if (strBrand.compareToIgnoreCase("Samsung") == 0 || strManufacturer.compareToIgnoreCase("Samsung") == 0) {
            return true;
        }
        return false;
    }
}