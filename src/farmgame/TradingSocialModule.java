package farmgame;

import java.util.*;

public class TradingSocialModule {
    public static class Item {
        private final String name;
        private final int price;
        public Item(String n,int p){name=n;price=p;}
        public String getName(){return name;}
        public int getPrice(){return price;}
        @Override public boolean equals(Object o){if(this==o)return true; if(!(o instanceof Item))return false; return name.equals(((Item)o).name);}
        @Override public int hashCode(){return Objects.hash(name);}
    }

    public static class Inventory{
        private final Map<Item,Integer> items=new HashMap<>();
        public int qty(Item i){return items.getOrDefault(i,0);}
        public void add(Item i,int q){items.put(i,qty(i)+q);}
        public boolean remove(Item i,int q){int cur=qty(i); if(cur<q)return false; if(cur==q)items.remove(i); else items.put(i,cur-q); return true;}
        public Map<Item,Integer> snapshot(){return new HashMap<>(items);}
    }

    public static class Player{
        private final String username;
        private int money;
        private final Inventory inv=new Inventory();
        private final SocialManager social=new SocialManager();
        public Player(String u,int m){username=u;money=m;}
        public String user(){return username;}
        public int money(){return money;}
        public Inventory inventory(){return inv;}
        public SocialManager social(){return social;}
        public boolean pay(int v){if(money<v)return false; money-=v; return true;}
        public void earn(int v){money+=v;}
    }

    public enum RelationState{STRANGER,ACQUAINTANCE,FRIEND,BEST_FRIEND,SOULMATE}

    public static class SocialManager{
        private final Map<String,Integer> playerLevels=new HashMap<>();
        private final Map<String,Integer> npcLevels=new HashMap<>();
        public int levelWithPlayer(String p){return playerLevels.getOrDefault(p,0);}
        public int levelWithNpc(String n){return npcLevels.getOrDefault(n,0);}
        public void talkToPlayer(String p){playerLevels.put(p,levelWithPlayer(p)+1);}
        public void talkToNpc(String n){npcLevels.put(n,levelWithNpc(n)+1);}
        public void giftToPlayer(String p,Item i){Objects.requireNonNull(i);playerLevels.put(p,levelWithPlayer(p)+2);}
        public void giftToNpc(String n,Item i){Objects.requireNonNull(i);npcLevels.put(n,levelWithNpc(n)+2);}
        public void completeQuest(String n){npcLevels.put(n,levelWithNpc(n)+5);}
        public RelationState state(int lvl){if(lvl>=100)return RelationState.SOULMATE; if(lvl>=70)return RelationState.BEST_FRIEND; if(lvl>=40)return RelationState.FRIEND; if(lvl>=10)return RelationState.ACQUAINTANCE; return RelationState.STRANGER;}
        public RelationState relationWithPlayer(String p){return state(levelWithPlayer(p));}
        public RelationState relationWithNpc(String n){return state(levelWithNpc(n));}
        public void dailyDecay(){playerLevels.replaceAll((k,v)->Math.max(0,v-1)); npcLevels.replaceAll((k,v)->Math.max(0,v-1));}
    }

    public static class TradeOffer{
        public enum Status{OPEN,ACCEPTED,DECLINED,CANCELED}
        private final Player from;
        private final Player to;
        private final Map<Item,Integer> offerItems;
        private final int offerMoney;
        private Status status=Status.OPEN;
        public TradeOffer(Player f,Player t,Map<Item,Integer> items,int money){from=f;to=t;offerItems=new HashMap<>(items);offerMoney=money;}
        public Player getFrom(){return from;}
        public Player getTo(){return to;}
        public Status getStatus(){return status;}
        private boolean transfer(){for(var e:offerItems.entrySet()){if(!from.inventory().remove(e.getKey(),e.getValue()))return false;} if(!to.pay(offerMoney)){return false;} from.earn(offerMoney); offerItems.forEach((i,q)->to.inventory().add(i,q)); return true;}
        public boolean accept(){if(status!=Status.OPEN)return false; if(!transfer())return false; status=Status.ACCEPTED; from.social().talkToPlayer(to.user()); to.social().talkToPlayer(from.user()); return true;}
        public boolean decline(){if(status!=Status.OPEN)return false; status=Status.DECLINED; return true;}
        public boolean cancel(){if(status!=Status.OPEN)return false; status=Status.CANCELED; return true;}
    }

    public static class TradeManager{
        private final List<TradeOffer> offers=new ArrayList<>();
        public TradeOffer propose(Player from,Player to,Map<Item,Integer> items,int money){TradeOffer o=new TradeOffer(from,to,items,money); offers.add(o); return o;}
        public List<TradeOffer> offersFor(Player p){List<TradeOffer> list=new ArrayList<>(); for(TradeOffer o:offers){if(o.getTo()==p&&o.getStatus()==TradeOffer.Status.OPEN)list.add(o);} return list;}
    }
}
