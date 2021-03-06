/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ib.order;

import com.ib.api.IBClient;
import org.apache.log4j.Logger;
import com.ib.quote.QuoteManager;
import com.ib.position.*;
import java.util.List;
//import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Siteng Jin
 */
public class Trader {
    private static final Logger LOG = Logger.getLogger(OrderManager.class);
    
    public static final Object ORDERCANCELMONITORLOCK = new Object();
    public static final Object ORDERCANCELMONITORLOCKFORWRAPPER = new Object();
    public static final Object ORDERMONITORLOCK = new Object();
    public static final Object OPENORDERENDLOCK = new Object();
    public static final Object FIRSTOPENORDERRECOREXECDLOCK = new Object();
    
    private IBClient m_client = null;
    
    public Trader(IBClient client){
        m_client = client;
    }
    
    
    public void startTrade(){
        try{
            // 1. Checking open orders
            OrderManager orderManager = m_client.getOrderManager();
            PositionManager positionManager = m_client.getPositionManager();
            QuoteManager quoteManager = m_client.getQuoteManager();
            
            new Thread(m_client.getCancelHandler(), "cancel monitor").start();
            
            positionManager.requestPosition();
            if(!positionManager.confirmAllPositionReceived()){
                // TODO
            }
            
            quoteManager.requestSourceData();
            if(!quoteManager.confirmTickTypesReceived()){
                // TODO
            }
            
            orderManager.requestOpenOrder();
            
            new Thread(m_client.getPositionMonitor(), "position monitor").start();
            
            new Thread(m_client.getOrderHandler(), "order monitor").start();
            
            Thread.sleep(500);
            
            orderManager.triggerOrderMonitor();
        } catch (Exception e){
            LOG.debug(e.getMessage(), e);
        }
    }
}
