import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;

public class Client{
    public static void main(String[] args) {

        //initialise userid and option choice
        Integer userid = 0;
        int option = 0;

        //generate pair of keys for client
        KeyPairGenerator gen = null;
        try {
            gen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        //create clients private key
        PrivateKey clientprivkey = pair.getPrivate();
        //create clients public key
        PublicKey clientpubkey = pair.getPublic();
        
        TokenInfo temptoken;
        String token = null; 

        try 
        {
            //connect to FrontEnd
            String name = "FrontEnd";
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction FrontEnd = (Auction) registry.lookup(name);

            //check if the user has exited.
            while (option != 7)
            {
                //scan for clients option choice
                Scanner uscanner = new Scanner(System.in);
                System.out.println();                
                System.out.println("Enter option");
                System.out.println("1 = Register");
                System.out.println("2 = Display item information");
                System.out.println("3 = Create new auction");
                System.out.println("4 = List available items");
                System.out.println("5 = Close auction");
                System.out.println("6 = Bid on auction");
                System.out.println("7 = EXIT");
                System.out.println();
                option = Integer.parseInt(uscanner.nextLine());
                System.out.println();

                //register
                if (option == 1)
                {
                    //take users input for email
                    uscanner = new Scanner(System.in);
                    System.out.println("Enter email");
                    System.out.println("Email requires @ and .");
                    String scannerEmail = (uscanner.nextLine());

                    //register user
                    userid = FrontEnd.register(scannerEmail, clientpubkey);

                    //error handling
                    if (userid == null)
                    {
                        System.out.println("Error with registration");
                    }
                    else
                    {
                        System.out.println("Registration completed");
                    }
                    
                }

                //getspec
                if (option == 2)
                {
                    // //make new token
                    // temptoken = getToken(userid, clientprivkey ,FrontEnd);
                    // token = temptoken.token;

                    //take users input for itemid
                    uscanner = new Scanner(System.in);
                    System.out.println("Enter itemID");
                    if (uscanner.hasNextInt())
                    {
                        int scannerItem = Integer.parseInt(uscanner.nextLine());

                        AuctionItem aitem = FrontEnd.getSpec(userid, scannerItem, token);

                        //error handling
                        if (aitem == null)
                        {
                            System.out.println("Error receiving item");
                        }
                        else
                        {
                            //print auctionitem properties
                            System.out.println("ItemID = " + aitem.itemID);
                            System.out.println("Name = " + aitem.name);
                            System.out.println("Description = " + aitem.description);
                            System.out.println("Highest Bid = " + aitem.highestBid);
                        }
                    }
                    else
                    {
                        System.out.println("Input has to be an number");
                    }
                    
                }

                //newauction
                if (option == 3)
                {
                    if (userid != 0)
                    {
                        // //make new token
                        // temptoken = getToken(userid, clientprivkey, FrontEnd);
                        // token = temptoken.token;

                        //create a new empty auctionsaleitem
                        AuctionSaleItem asi = new AuctionSaleItem();

                        //take users input for item name
                        uscanner = new Scanner(System.in);
                        System.out.println("Enter item name");
                        String sn = (uscanner.nextLine());
                        //add to auctionsaleitem object
                        asi.name = sn;
                        
                        //take users input for item description
                        System.out.println("Enter description");
                        String sd = (uscanner.nextLine());
                        //add to auctionsaleitem object
                        asi.description = sd;


                        //take users input for highest bid 
                        uscanner = new Scanner(System.in);
                        System.out.println("Enter starting price");
                        if (uscanner.hasNextInt())
                        {
                            Integer shb = Integer.parseInt(uscanner.nextLine());
                            //add to auctionsaleitem object
                            asi.reservePrice = shb;

                            //recieve itemid of new auction made
                            Integer uitemid = FrontEnd.newAuction(userid, asi, token);

                            //error handling
                            if (uitemid == null)
                            {
                                System.out.println("Error making item");
                            }
                            else
                            {
                                System.out.println("New auction item created. ItemID = " + uitemid);
                            }
                        }
                        else
                        {
                            System.out.println("Input has to be an number");
                        }
                    }
                    else
                    {
                        System.out.println("Register first");
                    }
                    
                }

                //listitems
                if (option == 4)
                {
                    // //make new token
                    // temptoken = getToken(userid, clientprivkey ,FrontEnd);
                    // token = temptoken.token;

                    //initialise array of auctionitems with returned values
                    AuctionItem[] aia = FrontEnd.listItems(userid, token);

                    //error handling
                    if (aia == null)
                    {
                        System.out.println("Error receiving item list");
                    }
                    else
                    {
                        //print item details for each item available 
                        for (AuctionItem aitem : aia)
                        {
                            System.out.println("ItemID = " + aitem.itemID);
                            System.out.println("Name = " + aitem.name);
                            System.out.println("Description = " + aitem.description);
                            System.out.println("Highest Bid = " + aitem.highestBid);
                            System.out.println();
                        }
                    }
                }

                //closeauction
                if (option == 5)
                {
                    if (userid != 0)
                    {
                        // //make new token
                        // temptoken = getToken(userid, clientprivkey ,FrontEnd);
                        // token = temptoken.token;

                        //take users input for itemid 
                        uscanner = new Scanner(System.in);
                        System.out.println("Enter itemID");
                        if (uscanner.hasNextInt())
                        {
                            int itemid = Integer.parseInt(uscanner.nextLine());

                            //close auction
                            AuctionResult result = FrontEnd.closeAuction(userid, itemid, token);

                            if (result == null)
                            {
                                System.out.println("Error closing auction");
                            }
                            else
                            {
                                //print results
                                System.out.println("Item " + itemid + " has been closed");
                                System.out.println("Winner email = " + result.winningEmail);
                                System.out.println("Winning price = " + result.winningPrice);
                            }
                        }
                        else
                        {
                            System.out.println("Input has to be an number");
                        }
                    }
                    else
                    {
                        System.out.println("Register first");
                    }
                    
                }

                //bid
                if (option == 6)
                {
                    if (userid != 0)
                    {
                        // //make new token
                        // temptoken = getToken(userid, clientprivkey ,FrontEnd);
                        // token = temptoken.token;

                        //take users input for itemid
                        uscanner = new Scanner(System.in);
                        System.out.println("Enter itemID");
                        if (uscanner.hasNextInt())
                        {
                            int itemid = Integer.parseInt(uscanner.nextLine());

                            //take users input for bid price
                            System.out.println("Enter price");
                            int price = Integer.parseInt(uscanner.nextLine());

                            //receive FrontEnd bid function to see if it was processed or not
                            Boolean result = FrontEnd.bid(userid, itemid, price, token);
                            if (result == true)
                            {
                                System.out.println("Bid has been processed");
                            }
                            //error handling
                            else 
                            {
                                System.out.println("Bid has not been processed");
                            }
                        }
                        else
                        {
                            System.out.println("Input has to be an number");
                        }
                    }
                    else
                    {
                        System.out.println("Register first");
                    }
                    
                }
            }
            System.exit(0);
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}