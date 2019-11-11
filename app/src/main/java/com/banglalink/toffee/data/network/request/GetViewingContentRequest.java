package com.banglalink.toffee.data.network.request;


import com.banglalink.toffee.data.storage.Preference;

/**
 * Created by shantanu on 12/5/16.
 */

public class GetViewingContentRequest extends BaseRequest{
    public String type;
    public int contentId;
    public int customerId;
    public String password;
    public String lat;
    public String lon;

    public GetViewingContentRequest(){
        super("viewingContent");
        password = Preference.Companion.getInstance().getPassword();
        customerId = Preference.Companion.getInstance().getCustomerId();
        lat = Preference.Companion.getInstance().getLatitude();
        lon = Preference.Companion.getInstance().getLongitude();
    }
}
