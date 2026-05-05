package com.auction.shared.network;

import com.google.gson.Gson;

// Class Message dùng để chuỗi hoá đối tượng vào JSON và ngược lại

public class Message {
    private String action;  // Dùng để mô tả hành động như LOGIN,updateProduct......
    private Object payload; // Dùng Object để chứa bất kỳ dữ liệu gì (User, Product, Bid...)


    public Message(String action, Object payload) {
        this.action = action;
        this.payload = payload;
    }

    //Getter
    public String getAction() { return action;}
    public Object getPayload() {return payload;}

    // Chuyển Object thành JSON:
    public String toJson(){
        return  new Gson().toJson(this);
    }

    // Chuyển JSON về Object:
    public static Message fromJson(String json){
        return new Gson().fromJson(json,Message.class);
    }
}

