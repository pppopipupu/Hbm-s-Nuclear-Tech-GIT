package com.hbm.packet.toclient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import com.hbm.main.ModEventHandlerClient;

public class QGPDistortionPacket implements IMessage {

	public int ticks;

	public QGPDistortionPacket() { }

	public QGPDistortionPacket(int ticks) {
		this.ticks = ticks;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.ticks = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.ticks);
	}

	public static class Handler implements IMessageHandler<QGPDistortionPacket, IMessage> {

		@Override
		public IMessage onMessage(QGPDistortionPacket message, MessageContext ctx) {
			ModEventHandlerClient.qgpDistortionTicks = message.ticks;
			return null;
		}
	}
}
