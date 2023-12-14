import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Replica implements ReplicaInterface{
    //ArrayList<AuctionItem> itemList = new ArrayList<AuctionItem>();
    LinkedList<AuctionItem> itemList = new LinkedList<AuctionItem>();
    //usermap : int = userid, user = string email
    Map<Integer, String> userMap = new HashMap<>();
    //userauctions : int = itemid, int = userid
    Map<Integer, Integer> userAuctions = new HashMap<>();
    //arraylist that contains userid of highest bid, bid price and itemid
    ArrayList<AuctionBidder> auBidder = new ArrayList<AuctionBidder>();

    int useridcount;
    int itemcount;

    //name and id of replica
    int replicaid;
    String replicaName;

    //arraylist of available replicas
    ArrayList<ReplicaInterface> replicaList = new ArrayList<>();;

    //flag to check what replica is primary replica
    private boolean primaryreplicaflag;

    public Replica() {
        super();

        //initalise number of users and number of items and set primary replica flag to false by default
        useridcount = 0;
        itemcount = 0;
        primaryreplicaflag = false;
    }

    public synchronized Integer register(String email, PublicKey pubKey)
    {
        //check if email is in valid format
        if (email.contains("@") && email.contains("."))
        {
            //check to make sure that email is unique and isnt already inside the user hashmap
            try
            {
                for (String e: userMap.values())
                {
                    if (email.equals(e))
                    {
                        System.out.println("email already exists");
                        return null;
                    }
                }
                //increment id count and add data to user hashmap and userkey hashmap
                useridcount = useridcount+1;
                userMap.put(useridcount, email);

                //set and update replica servers
                setReplicas();

                //return userid
                return useridcount;
            }catch (Exception e)
            {
                return null; 
            }
        }
        else
        {
            return null;
        }
    }

    public synchronized AuctionItem getSpec(int userID, int itemID, String token)
    {
        try
        {
            //go through the list of items and return the correct item
            for (int i = 0; i < itemList.size(); i++)
            {
                if (itemID == itemList.get(i).itemID)
                {
                    return itemList.get(i);  
                }
            }
            return null;
        }catch (Exception e)
        {
            // System.err.println("itemID doesnt exist:");
            return null;
        }
    }

    public synchronized Integer newAuction(int userID, AuctionSaleItem item, String token)
    {
        try
        {
            //check if user hasn't registered
            if (userID == 0)
            {
                // System.out.println("Register First");
                return null;
            } 

            //make a new auctionitem
            AuctionItem au = new AuctionItem();

            //seperate auction item from user
            String n = item.name;
            String d = item.description;
            int rp = item.reservePrice;
            
            //increment number of items by 1
            itemcount = itemcount + 1;

            //get empty item and add data
            au.itemID = (itemcount);
            au.name = n;
            au.description = d;
            au.highestBid = rp;

            //add item to item list
            itemList.add(au);

            //add item and userid of creator to hashmap
            userAuctions.put(itemcount, userID);

            //add creator to arraylist of the item with start price as the first bidder
            auBidder.add(new AuctionBidder(userID, itemcount, rp));
            
            //set and update replica servers
            setReplicas();         

            //return id of item
            return itemcount;

        }catch (Exception e)
        {
            return null;
        }
    }

    public synchronized AuctionItem[] listItems(int userID, String token)
    {
        //create a new array of type auctionitem and add all items from arraylist.
        AuctionItem[] aia = new AuctionItem[itemList.size()];
        itemList.toArray(aia);

        //return array
        return aia;
    }

    public synchronized AuctionResult closeAuction(int userID, int itemID, String token)
    {
        //check if user hasn't registered
        if (userID == 0)
        {
            // System.out.println("Register First");
            return null;
        } 
        try {
            //check if the userid is the owner of the auction
            if (userID == userAuctions.get(itemID))
            {
                //create a new winner of the auction
                AuctionResult winner = new AuctionResult();
                //go through arraylist of auctions with bidder information
                for (int i = 0; i < auBidder.size(); i++)
                {
                    //check for the item to be closed
                    if (itemID == auBidder.get(i).itemID)
                    {
                        //get the user from the userid and compare it to get the winners email.
                        int tempuid = auBidder.get(i).userID;
                        String tempemail = userMap.get(tempuid);
                        
                        //go through the list of items
                        for (int x = 0; x < itemList.size(); x++)
                        {
                            //if itemid matches 
                            if (itemID == itemList.get(x).itemID)
                            {
                                //save items highest bid in variable
                                int temphighestbid = itemList.get(x).highestBid;

                                //add data into empty winner
                                winner.winningEmail = tempemail;
                                winner.winningPrice = temphighestbid;

                                //delete data for item as it no longer is needed
                                itemList.remove(x);
                                auBidder.remove(i);

                                //set and update replica servers
                                setReplicas(); 

                                //return winner
                                return winner;
                            }
                        }
                    }
                }
            }
            else
            {
                return null; 
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    
    public synchronized boolean bid(int userID, int itemID, int price, String token)
    {
        //check if user hasn't registered
        if (userID == 0)
        {
            // System.out.println("Register First");
            return false;
        } 
        //check if userid owns the item to be bid on
        else if (userID == userAuctions.get(itemID))
        {
            // System.out.println("Auction owner cannot bid on their own auction");
            return false;
        }
        else
        {
            //go through the array of auction bidders
            for (int i = 0; i < auBidder.size(); i++)
            {
                //check if the item wanting to be bidded on has been found
                if (itemID == auBidder.get(i).itemID)
                {
                    //check if the users bid price is more than the current price of the auction
                    if (auBidder.get(i).price < price)
                    {
                        //replace userid and price of the auction item
                        auBidder.get(i).userID = userID;
                        auBidder.get(i).price = price;

                        //update the items displayable price in the itemlist arraylist
                        for (int x = 0; x < itemList.size(); x++)
                        {
                            if (itemID == itemList.get(x).itemID)
                            {
                                itemList.get(x).highestBid = price;
                            }
                        }

                        //set and update replica servers
                        setReplicas();

                        return true;
                    }
                    else
                    {
                        // System.out.println("price has to be higher than the one given");
                        return false;
                    }
                }
                else
                {
                    // System.out.println("item doesnt exist");
                    return false;
                }
            }
        }
        return false;
    }

    public int getPrimaryReplicaID()
    {
        // return primary replica id
        return this.replicaid;
    }

    public synchronized void setPrimaryReplicaFlag()
    {
        // toggle primary replica flag
        primaryreplicaflag = !primaryreplicaflag;
    }

    public synchronized boolean getPrimaryReplicaFlag()
    {
        // return the primary replica flag to see what replica is the primary
        return primaryreplicaflag;
    }

    public synchronized String[] listingstuff()
    {
        try {
        
            Registry registry = LocateRegistry.getRegistry();

            // List everything available in rmiregistry
            String[] rlist = registry.list();

            // clear current list or replicas
            replicaList.clear();

            // return registry list
            return rlist;

        } catch (Exception e){
            
        }
        return null;
    }

    public synchronized void setnewReplica(String[] r)
    {
        try {
            Registry registry = LocateRegistry.getRegistry();

            String[] rlist = registry.list();
            for (String val:rlist)
            {
                if (!val.startsWith("Replica") || val == "FrontEnd")
                {
                    break;
                }

                //find primary replica through rmiregistry using primary replica flag and name
                ReplicaInterface replica = (ReplicaInterface) registry.lookup(val);
                if (replica.getPrimaryReplicaFlag() == true && val.startsWith("Replica")){
                {
                    try {
                        // update newly made replica with state of primary replica
                        updateState(replica.getItemList(), replica.getUserMap(), replica.getUserAuctions(), replica.getAuBidder(), replica.getUserIdCount(), replica.getItemCount());
                    } catch (Exception e) {
                        // System.out.println("error with updating the new replica");
                    }
                } 
                }
            }
        } catch (RemoteException | NotBoundException e) {
            // System.out.println("issue with setting up new replica");
        }
    }

    public synchronized LinkedList<AuctionItem> getItemList()
    {
        return itemList;
    }

    public synchronized Map<Integer, String> getUserMap()
    {
        return userMap;
    }

    public synchronized Map<Integer, Integer> getUserAuctions()
    {
        return userAuctions;
    }

    public synchronized ArrayList<AuctionBidder> getAuBidder()
    {
        return auBidder;
    }

    public synchronized int getUserIdCount()
    {
        return useridcount;
    }

    public synchronized int getItemCount()
    {
        return itemcount;
    }

    public synchronized void setReplicas()
    {
        try {
            Registry registry = LocateRegistry.getRegistry();

            String[] rlist = registry.list();

            //clear list of replicas
            replicaList.clear();

            //update all replicas that arent the primary replica
            for (String val:rlist){
                if(val.startsWith("Replica") && !val.equals(replicaName)){
                    ReplicaInterface replica = (ReplicaInterface) registry.lookup(val);
                    replicaList.add(replica);
                }
            }
            
            //go though each of the replicas in the list that arent the primary replica and update them
            for (ReplicaInterface r : replicaList) {
                r.updateState(itemList, userMap, userAuctions, auBidder, useridcount, itemcount);
            }
        } catch (RemoteException | NotBoundException e) {
            // System.out.println("error with setreplicas");
            // e.printStackTrace();
        }
    }


    public synchronized void updateState(LinkedList<AuctionItem> itemList, Map<Integer, String> userMap, Map<Integer, Integer> userAuctions, ArrayList<AuctionBidder> auBidder, int useridcount, int itemcount) 
    {
        this.itemList = itemList;
        this.userMap = userMap;
        this.userAuctions = userAuctions;
        this.auBidder = auBidder;
        this.useridcount = useridcount;
        this.itemcount = itemcount;
    }

    public synchronized boolean ping()
    {
        // return true if ping to the replica has worked = replica is alive
        return true;
    }

    
    public static void main(String[] args) {
    try {
        int rid = 3;
        try {
            if (args[0].equals("0")){
                // System.out.println("arg cannot be 0");
            }
            else
            {
                rid = Integer.parseInt(args[0]);
            }
        } catch (Exception e) {
            // TODO: handle exception
            // System.out.println("arg hasnt worked");
        }
        
        // String replicaName = "Replica" + rid;
        Replica r = new Replica();
        r.replicaid = rid;
        r.replicaName = "Replica" + rid;
        String[] something = r.listingstuff();
        r.setnewReplica(something);

        ReplicaInterface stub = (ReplicaInterface) UnicastRemoteObject.exportObject(r, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(r.replicaName, stub);
        System.out.println(r.replicaName + " Ready");
        } catch (Exception e) 
        {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}