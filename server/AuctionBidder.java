public class AuctionBidder implements java.io.Serializable {
    int userID;
    int itemID;
    int price;

    public AuctionBidder(int uid, int iid, int p)
    {
        this.userID = uid;
        this.itemID = iid;
        this.price = p;
    }
}
