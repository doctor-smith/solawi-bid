package org.solyton.bid.module.bid.data.api

import kotlinx.serialization.json.Json
import org.solyton.solawi.bid.module.bid.data.api.AuctionDetails
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTests {

    @Test fun serializeAuctionDetails() {
        val details = AuctionDetails.SolawiTuebingen(
            0.0,0.0,0.0,0.0,
        )
        val serialized = Json.encodeToString(AuctionDetails.serializer(), details)
        val deserialized = Json.decodeFromString(AuctionDetails.serializer(), serialized)

        assertEquals(details, deserialized)

        val empty = AuctionDetails.Empty
        val serializedE = Json.encodeToString(AuctionDetails.serializer(), empty)
        val deserializedE = Json.decodeFromString(AuctionDetails.serializer(), serializedE)

        assertEquals(empty, deserializedE)
    }
}