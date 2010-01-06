package org.googlecode.vkontakte_android.utils;

import org.googlecode.vkontakte_android.R;

public final class ProfileInfoHelper {
    public static final int SEX_FEMALE = 1;

    public static int getPoliticalViewId(int id) {
        int stringId;
        switch (id) {
            case 1:
                stringId = R.string.pv_communist;
                break;
            case 2:
                stringId = R.string.pv_socialist;
                break;
            case 3:
                stringId = R.string.pv_moderate;
                break;
            case 4:
                stringId = R.string.pv_liberal;
                break;
            case 5:
                stringId = R.string.pv_conservative;
                break;
            case 6:
                stringId = R.string.pv_monarchist;
                break;
            case 7:
                stringId = R.string.pv_ultraconservative;
                break;
            case 8:
                stringId = R.string.pv_apathetic;
                break;
            default:
                stringId = -1;
        }
        return stringId;
    }

    public static int getFamilyStatusId(int id, int sex) {
        int stringId;
        switch (id) {
            case 1:
                if (sex == SEX_FEMALE) {
                    stringId = R.string.fs_single_female;
                } else {
                    stringId = R.string.fs_single_male;
                }
                break;
            case 2:
                stringId = R.string.fs_relationship;
                break;
            case 3:
                if (sex == SEX_FEMALE) {
                    stringId = R.string.fs_engaged_female;
                } else {
                    stringId = R.string.fs_engaged_male;
                }
                break;
            case 4:
                if (sex == SEX_FEMALE) {
                    stringId = R.string.fs_married_female;
                } else {
                    stringId = R.string.fs_married_male;
                }
                break;
            case 5:
                stringId = R.string.fs_complicated;
                break;
            case 6:
                stringId = R.string.fs_as;
                break;
            default:
                stringId = -1;
                break;
        }
        return stringId;
    }

}
