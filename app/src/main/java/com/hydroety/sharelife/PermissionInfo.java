package com.hydroety.sharelife;


public class PermissionInfo {
    private String mPermissionId;
    private String mType;
    private String mAddress;
    private String mName;
    private String mRole;
    private String mPhotoLink;
    private boolean mIsDeleted=false;

    public void setPermissionId(String Id) {
        mPermissionId = Id;
    }
    public String getPermissionId() {
        return mPermissionId;
    }

    public void setType(String Type) {
        mType = Type;
    }
    public String getType() {
        return mType;
    }

    public void setAddress(String Address) {
        mAddress = Address;
    }
    public String getAddress() {
        return mAddress;
    }

    public void setName(String Name) {
        mName = Name;
    }
    public String getName() {
        return mName;
    }

    public void setRole(String Role) {
        mRole = Role;
    }
    public String getRole() {
        return mRole;
    }

    public void setPhotoLink(String PhotoLink) {
        mPhotoLink = PhotoLink;
    }
    public String getPhotoLink() {
        return mPhotoLink;
    }

    public void setDeleted(boolean deleted) {
        mIsDeleted = deleted;
    }
    public boolean IsDeleted() {
        return mIsDeleted;
    }

    public boolean equals(Object obj){

        PermissionInfo t = (PermissionInfo)obj;
        return mAddress.equals(t.getAddress());

    }


}
