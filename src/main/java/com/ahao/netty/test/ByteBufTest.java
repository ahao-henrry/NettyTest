package com.ahao.netty.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBufTest {
    
    public static void main(String[] args) {
        new ByteBufTest().zbi();
    }
    
    public void zbi() {
        //create a ByteBuf of capacity is 16
        ByteBuf buf = Unpooled.buffer(16);
        //write data to buf
        for(int i=0;i<16;i++){
            buf.writeByte(i+1);
        }
        //read data from buf
        for(int i=0;i<buf.capacity();i++){
            System.out.println(buf.getByte(i));
        }
    }
}
