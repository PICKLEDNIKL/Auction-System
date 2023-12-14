import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FrontEnd implements Auction{

    private ReplicaInterface primaryreplica;
    private String primaryreplicaname;

    public FrontEnd() {
        super();
    }

    public synchronized Integer register(String email, PublicKey pubKey)
    {
        try {
            //if ping doesnt work then change primary replica
            if (callping() == null)
            {
                changePrimaryReplica();
            }
            Integer userid = primaryreplica.register(email, pubKey);
            return userid;
        } catch (RemoteException | NotBoundException e) {

            return null;
        }   
    }

    public synchronized ChallengeInfo challenge(int userID, String serverChallenge)
    {
        return null;
    }

    public synchronized TokenInfo authenticate(int userID, byte signature[])
    {
        return null;  
    }

    public synchronized AuctionItem getSpec(int userID, int itemID, String token)
    {
        try {
            //if ping doesnt work then change primary replica
            if (callping() == null)
            {
                changePrimaryReplica();
            }
            AuctionItem item = primaryreplica.getSpec(userID, itemID, token);
            return item;
        } catch (RemoteException | NotBoundException e) {

            return null;
        }   
    }

    public synchronized Integer newAuction(int userID, AuctionSaleItem item, String token)
    {
        try {
            //if ping doesnt work then change primary replica
            if (callping() == null)
            {
                changePrimaryReplica();
            }
            Integer itemid = primaryreplica.newAuction(userID, item, token);
            return itemid;
        } catch (RemoteException | NotBoundException e) {

            e.printStackTrace();
            return null;
        }   
    }

    public synchronized AuctionItem[] listItems(int userID, String token)
    {
        try {
            //if ping doesnt work then change primary replica
            if (callping() == null)
            {
                changePrimaryReplica();
            }
            AuctionItem[] aia = primaryreplica.listItems(userID, token);
            return aia;
        } catch (RemoteException | NotBoundException e) {

            return null;
        }   
    }

    public synchronized AuctionResult closeAuction(int userID, int itemID, String token)
    {
        try {
            //if ping doesnt work then change primary replica
            if (callping() == null)
            {
                changePrimaryReplica();
            }
            AuctionResult winner = primaryreplica.closeAuction(userID, itemID, token);
            return winner;
        } catch (RemoteException | NotBoundException e) {

            return null;
        }   
    }

    public synchronized boolean bid(int userID, int itemID, int price, String token)
    {
        boolean bidresult;

        try {
            //if ping doesnt work then change primary replica
            if (callping() == null)
            {
                changePrimaryReplica();
            }
            bidresult = primaryreplica.bid(userID, itemID, price, token);
            return bidresult;
        } catch (RemoteException | NotBoundException e) {

            return false;
        }
    }

    public synchronized int getPrimaryReplicaID()
    {
        int id = 0;
        try {
            //if ping doesnt work then change primary replica
            if (callping() == null)
            {
                changePrimaryReplica();
            }
            id = primaryreplica.getPrimaryReplicaID();
            return id;
        } catch (RemoteException | NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // System.out.println("this is the id of the primary replica " + id);
        return id;
    }

    public synchronized ReplicaInterface callping() throws RemoteException, NotBoundException
    {
        Registry registry = LocateRegistry.getRegistry();

        // check if ping works and if it doesnt then unbind it from registry
        try {
            if (primaryreplica.ping() == true)
            {
                // System.out.println("ping has worked");
                return primaryreplica;
            }
        } catch (Exception e) {
            // System.out.println("ping has not worked");

            registry.unbind(primaryreplicaname);
            System.out.println(primaryreplicaname + " : unbinded");
            return null;
        }
        return null;
    }

    public synchronized void changePrimaryReplica() throws RemoteException, NotBoundException
    {
        Registry registry = LocateRegistry.getRegistry();

        // check registry list to get all the replicas and test ping on all of them
        String[] unbindrlist = registry.list();
        for (String val: unbindrlist)
        {
            if (val.startsWith("Replica") && !val.equals("FrontEnd"))
            {
                ReplicaInterface callpingtest = (ReplicaInterface) registry.lookup(val);
                primaryreplica = callpingtest;
                primaryreplicaname = val;
                // System.out.println(primaryreplicaname);
                callping();
                //other option is to ping all of the different replicas and unbind the ones that cant ping?
            }
        }

        String[] rlist = registry.list();

        List<String> knownreplicas = new ArrayList<String>();

        // add available replicas to list
        for (String val:rlist){
            if(val.startsWith("Replica")){
                knownreplicas.add(val);
            }
        }

        String primaryreplicaname = null;

        // select random available replica as the primary replica
        try {
            Random rnd = new Random();
            int rndindex = rnd.nextInt(knownreplicas.size());
            primaryreplicaname = knownreplicas.get(rndindex);
            
        } catch (Exception e) {
            // System.out.println("replicas dont exist");
        }

        System.out.println("Primary replica = " + primaryreplicaname);

        // set old primary replica as new primary replica
        ReplicaInterface tempprimaryreplica = (ReplicaInterface) registry.lookup(primaryreplicaname);
        this.primaryreplica = tempprimaryreplica;
        this.primaryreplicaname = primaryreplicaname;
        this.primaryreplica.setPrimaryReplicaFlag();
    }
    
    public static void main(String[] args) {
    try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            String name = "FrontEnd";

            //find replica through rmiregisttry and save to a list of replicas
            String[] rlist = registry.list();
            List<String> knownreplicas = new ArrayList<String>();
            for (String val:rlist){
                if(val.startsWith("Replica")){
                    knownreplicas.add(val);
                }
            }

            String primaryreplicaname = null;

            // select random available replica as the primary replica
            try {
                Random rnd = new Random();
                int rndindex = rnd.nextInt(knownreplicas.size());
                primaryreplicaname = knownreplicas.get(rndindex);
                
            } catch (Exception e) {
                System.out.println("replicas dont exist");
            }

            System.out.println("Primary replica = " + primaryreplicaname);

            ReplicaInterface tempprimaryreplica = (ReplicaInterface) registry.lookup(primaryreplicaname);
            System.out.println("FrontEnd ready");

            // set first primary replica
            FrontEnd fr = new FrontEnd();
            fr.primaryreplica = tempprimaryreplica;
            fr.primaryreplicaname = primaryreplicaname;
            fr.primaryreplica.setPrimaryReplicaFlag();

            Auction stub = (Auction) UnicastRemoteObject.exportObject(fr, 0);
            registry.rebind(name, stub);

        } catch (Exception e) 
        {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}