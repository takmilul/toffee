package com.banglalink.toffee.data.network.response;


/**
 * Created by shantanu on 12/5/16.
 */

public class GetViewingContentResponse extends BaseResponse{

    public GetViewingContent response;

    public class GetViewingContent extends BodyResponse{
        public int balance;
        public String systemTime;
    }
}
