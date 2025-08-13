package com.Client.dialogs;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.common.Network;
import com.esotericsoftware.kryonet.Client;

import java.util.HashMap;
import java.util.Map;

public class TradeDialog extends Dialog {
    private final Client client;
    private final String myUsername;

    private final TextField toField;
    private final TextField itemField;
    private final TextField amountField;
    private final TextField priceField;

    public TradeDialog(Skin skin, Client client, String myUsername) {
        super("New Trade", skin);
        this.client = client;
        this.myUsername = myUsername;

        toField     = new TextField("",     skin);
        itemField   = new TextField("Apple",skin);
        amountField = new TextField("1",    skin);
        priceField  = new TextField("10",   skin);

        // فقط عدد
        amountField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        priceField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());

        Table t = getContentTable();
        t.add(new Label("To (username):", skin)).left().row();
        t.add(toField).width(240).row();
        t.add(new Label("Item:", skin)).left().row();
        t.add(itemField).width(240).row();
        t.add(new Label("Amount:", skin)).left().row();
        t.add(amountField).width(240).row();
        t.add(new Label("Price:", skin)).left().row();
        t.add(priceField).width(240).row();

        button("Offer", true);
        button("Cancel", false);
        key(Input.Keys.ENTER, true);
        key(Input.Keys.ESCAPE, false);
    }

    @Override
    protected void result(Object obj) {
        if (!Boolean.TRUE.equals(obj)) return;

        Network.TradeOffer o = new Network.TradeOffer();
        o.from = myUsername;

        String toVal = toField.getText().trim();
        o.to = toVal.isEmpty() ? null : toVal; // اگر باید حتماً پر شود، این را چک کن و return بده

        String item = itemField.getText().trim();

        int amount;
        try { amount = Integer.parseInt(amountField.getText().trim()); }
        catch (Exception ignore) { amount = 1; }

        int price;
        try { price = Integer.parseInt(priceField.getText().trim()); }
        catch (Exception ignore) { price = 1; }

        // پر کردن پروتکل جدید:
        Map<String, Integer> bundle = new HashMap<>();
        if (!item.isEmpty()) bundle.put(item, amount);
        o.bundle = bundle;
        o.price  = price;

        client.sendTCP(o);
    }
}
