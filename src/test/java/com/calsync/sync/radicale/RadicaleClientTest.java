package com.calsync.sync.radicale;

import biweekly.ICalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RadicaleClientTest {
    private static final RadicaleClient radicaleClient = new RadicaleClient("https://welsione.cn/gaolei.wei/a601acaa-6d58-74ed-72ca-740e2bd983b6/","gaolei.wei","Wgl#1234");
    
    public static void main(String[] args) {
        System.out.println(radicaleClient.ping());
        System.out.println(radicaleClient.queryAll());
        List<ICalendar> iCalendars = radicaleClient.queryAll();
        for (ICalendar iCalendar : iCalendars) {
            System.out.println(iCalendar);
        }
    }
}