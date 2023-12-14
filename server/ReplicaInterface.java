import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public interface ReplicaInterface extends Remote {

    Map<Integer, String> getChallengeMap = null;
	public Integer register(String email, PublicKey pubKey) throws RemoteException;
    public AuctionItem getSpec(int userID, int itemID, String token) throws RemoteException;
    public Integer newAuction(int userID, AuctionSaleItem item, String token) throws
    RemoteException;
    public AuctionItem[] listItems(int userID, String token) throws RemoteException;
    public AuctionResult closeAuction(int userID, int itemID, String token) throws
    RemoteException;
    public boolean bid(int userID, int itemID, int price, String token) throws
    RemoteException;
    public int getPrimaryReplicaID() throws RemoteException;
    public void setReplicas() throws RemoteException;
    public void updateState(LinkedList<AuctionItem> itemList, Map<Integer, String> userMap, Map<Integer, Integer> userAuctions, ArrayList<AuctionBidder> auBidder, int useridcount, int itemcount) throws RemoteException;
    public boolean ping() throws RemoteException;
    public void setPrimaryReplicaFlag() throws RemoteException;
    public boolean getPrimaryReplicaFlag() throws RemoteException;
    public LinkedList<AuctionItem> getItemList() throws RemoteException;
    public Map<Integer, String> getUserMap() throws RemoteException;
	public Map<Integer, Integer> getUserAuctions() throws RemoteException;
	public ArrayList<AuctionBidder> getAuBidder() throws RemoteException;
	public int getUserIdCount() throws RemoteException;
	public int getItemCount() throws RemoteException;
}