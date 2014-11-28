package com.zeebo.libe

import com.google.gson.Gson
import com.zeebo.beryllium.OperationalTransform
import com.zeebo.lithium.mesh.Message
import com.zeebo.lithium.message.MessageHandler

/**
 * User: eric
 * Date: 11/28/14
 */
class OperationalTransformMessageHandler extends MessageHandler {

	public static final int TYPE_BERYLLIUM_OPERATIONAL_TRANSFORM = 15952
	public static final int TYPE_REQUEST_NEW_SITE_ID = 15953
	public static final int TYPE_RESPOND_NEW_SITE_ID = 15954

	static Gson gson
	LithiumOperationalTransformNetwork network

	def messageResponseMap = [:].withDefault { [] }

	@Override
	IntRange getTypeRange() { (15952..<15960) }

	def handleBerylliumOperationalTransform(Message message) {
		if (message.data.networkName == network.networkName) {
			OperationalTransform ot = message.data.trans as OperationalTransform

			network.site.receiveQueue << ot
			network.site.tryInvokeRemote()
		}
	}

	def handleRequestNewSiteId(Message message) {

		if (message.data.networkName == network.networkName) {
			Message ret = new Message()
			ret.messageType = TYPE_RESPOND_NEW_SITE_ID
			ret.data.responseId = message.messageId
			ret.data.networkName = network.networkName
			ret.data.siteId = network.sites.size()

			network.sites << message.sender
			network.site.sv << 0

			network.meshNode.send(message.sender, ret)
		}
	}

	def handleRespondNewSiteId(Message message) {
		if (message.data.networkName == network.networkName) {
			network.site.id = message.data.siteId
			(0..<network.site.id).each {
				network.site.sv << 0
			}
		}
	}
}
