package farmgame;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TradingSocialModule {
        public static class Item {
            private final String name;
            private final int basePrice;
            public Item(String n,int p){name=n;basePrice=p;}
            public String getName(){return name;}
            public int getBasePrice(){return basePrice;}
        }
        public static class Inventory{
            private final Map<Item,Integer> items=new HashMap<>();
            public int quantity(Item i){return items.getOrDefault(i,0);}
            public void add(Item i,int q){items.put(i,quantity(i)+q);}
            public boolean remove(Item i,int q){int cur=quantity(i);if(cur<q)return false; if(cur==q)items.remove(i);else items.put(i,cur-q);return true;}
        }
        public static class Player{
            private final String username;
            private int money;
            private final Inventory inventory=new Inventory();
            private final FriendshipManager friendships=new FriendshipManager();
            public Player(String u,int m){username=u;money=m;}
            public String getUsername(){return username;}
            public int getMoney(){return money;}
            public Inventory getInventory(){return inventory;}
            public FriendshipManager getFriendships(){return friendships;}
            public void addMoney(int v){money+=v;}
            public boolean spendMoney(int v){if(money<v)return false;money-=v;return true;}
        }
        public static class TradeManager{
            public static boolean buy(Player p,Item i,int q,int price){int total=price*q; if(!p.spendMoney(total))return false; p.getInventory().add(i,q); return true;}
        }
        public static class FriendshipManager{
            private final Map<String,Integer> levels=new HashMap<>();
            public int level(String npc){return levels.getOrDefault(npc,0);}
            public void talk(String npc){levels.put(npc,level(npc)+1);}
            public void gift(String npc,Item item){Objects.requireNonNull(item);levels.put(npc,level(npc)+2);}
            public void decayAll(){for(var e:levels.entrySet()){int v=Math.max(0,e.getValue()-1);e.setValue(v);} }
        }
    }


