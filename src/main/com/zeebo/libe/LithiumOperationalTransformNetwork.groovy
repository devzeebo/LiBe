package com.zeebo.libe

import com.zeebo.beryllium.OperationalTransform
import com.zeebo.beryllium.OperationalTransformNetwork
import com.zeebo.beryllium.OperationalTransformSite
import com.zeebo.lithium.mesh.MeshNode
import com.zeebo.lithium.mesh.Message

/**
 * User: eric
 * Date: 11/28/14
 */
class LithiumOperationalTransformNetwork extends OperationalTransformNetwork {

	static int siteId

	MeshNode meshNode

	String networkName
	OperationalTransformSite site

	LithiumOperationalTransformNetwork(MeshNode networkNode) {
		meshNode = networkNode
		meshNode.addMessageHandler new OperationalTransformMessageHandler(network: this)

		site = new OperationalTransformSite()
		site.id = siteId++
		site.network = this
		site.sv << 0

		sites << site
	}

	void joinMeshNetwork() {
		Message m1sid = new Message()
		m1sid.messageType = OperationalTransformMessageHandler.TYPE_REQUEST_NEW_SITE_ID
		meshNode.sendAll(m1sid)
	}

	void broadcast(int sendingNodeId, OperationalTransform trans) {
		Message message = new Message()
		message.messageType = OperationalTransformMessageHandler.TYPE_BERYLLIUM_OPERATIONAL_TRANSFORM
		message.data.networkName = networkName
		message.data.sendingNodeId = sendingNodeId
		message.data.trans = trans

		meshNode.sendAll(message)
	}

	public static void main(String[] args) {
		MeshNode n1 = new MeshNode('node1', 40026)
		MeshNode n2 = new MeshNode('node2', 40027)
		LithiumOperationalTransformNetwork a = new LithiumOperationalTransformNetwork(n1)
		LithiumOperationalTransformNetwork b = new LithiumOperationalTransformNetwork(n2)

		n1.listen()
		n2.listen()

		n1.connect('localhost', 40027)

		sleep 1000

		a.joinMeshNetwork()
		b.joinMeshNetwork()

		sleep 1000

		println a.site.sv
		println b.site.sv

		a.broadcast(0, a.site.createOperationalTransform(OperationalTransform.TYPE_INSERT, 'a', 0))

		sleep 1000

		println a.site.sv
		println b.site.sv
	}
}
