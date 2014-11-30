package com.zeebo.libe

import com.google.gson.Gson
import com.zeebo.beryllium.OperationalTransform
import com.zeebo.beryllium.OperationalTransformSite
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

		OperationalTransformSite site = network.localSite[message.data.networkName]

		println message.data

		if (site) {
			OperationalTransform ot = message.data.trans as OperationalTransform

			println ot.sv

			site.receiveQueue << ot
			site.tryInvokeRemote()
		}
	}

	def handleRequestNewSiteId(Message message) {

		OperationalTransformSite site = network.localSite[message.data.networkName]

		if (site) {
			Message ret = new Message()
			ret.messageType = TYPE_RESPOND_NEW_SITE_ID
			ret.data.responseId = message.messageId
			ret.data.networkName = message.data.networkName
			ret.data.siteId = network.networks[message.data.networkName]

			network.networks[message.data.networkName]++
			site.sv << 0

			network.meshNode.send(message.sender, ret)
		}
	}

	def handleRespondNewSiteId(Message message) {

		OperationalTransformSite site = network.localSite[message.data.networkName]

		if (site) {

			println "Site id in ${message.data.networkName} is now ${message.data.siteId as int}"

			site.id = message.data.siteId as int
			(0..<site.id).each {
				site.sv << 0
			}
		}
	}
}
