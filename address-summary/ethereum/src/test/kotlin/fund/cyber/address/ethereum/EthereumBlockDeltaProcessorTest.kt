package fund.cyber.address.ethereum

import fund.cyber.address.ethereum.summary.EthereumAddressSummaryDelta
import fund.cyber.address.ethereum.summary.EthereumBlockDeltaProcessor
import fund.cyber.search.model.chains.EthereumFamilyChain
import fund.cyber.search.model.ethereum.EthereumBlock
import fund.cyber.search.model.events.PumpEvent
import fund.cyber.search.model.events.blockPumpTopic
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant


@DisplayName("Ethereum block delta processor test: ")
class EthereumBlockDeltaProcessorTest {

    private val expectedDelta = EthereumAddressSummaryDelta(
            address = "0xea674fdde714fd979de3edf0f56aa9716b898ec8",
            balanceDelta = BigDecimal("5.089262252548096035"),
            contractAddress = null, totalReceivedDelta = BigDecimal("5.089262252548096035"), txNumberDelta = 0,
            uncleNumberDelta = 0, minedBlockNumberDelta = 1,
            topic = EthereumFamilyChain.ETHEREUM.blockPumpTopic, partition = 0, offset = 0
    )

    private val expectedDroppedDelta = EthereumAddressSummaryDelta(
            address = "0xea674fdde714fd979de3edf0f56aa9716b898ec8",
            balanceDelta = BigDecimal("5.089262252548096035").negate(),
            contractAddress = null, totalReceivedDelta = BigDecimal("5.089262252548096035").negate(),
            txNumberDelta = 0, uncleNumberDelta = 0, minedBlockNumberDelta = -1,
            topic = EthereumFamilyChain.ETHEREUM.blockPumpTopic, partition = 0, offset = 0
    )

    private val block = EthereumBlock(
            hash = " 0xa27c04a1f42b2e5264e5cfb0cd1ca6fb84c360cbef63ea1b171906b1018e16dd", number = 5386266,
            parentHash = "0x020d890a97901cae61e76d5375051b90ca8e5814a6ce775caecebac6f14d9236",
            txNumber = 158, miner = "0xea674fdde714fd979de3edf0f56aa9716b898ec8",
            difficulty = BigInteger("3076132037691991"),
            totalDifficulty = BigInteger("3468611771897182658973"), size = 23681,
            unclesReward = BigDecimal("1.875"), blockReward = BigDecimal("3"),
            txFees = BigDecimal("0.214262252548096035"), gasUsed = 5981414, gasLimit = 8000029,
            timestamp = Instant.now(), logsBloom = "", transactionsRoot = "", stateRoot = "",
            sha3Uncles = "0x8e087e9269dd7ba6cef2b2def746f07c06df35d9947480799ef38143301cc20e",
            nonce = 1, receiptsRoot = "", extraData = "", uncles = emptyList()
    )

    @Test
    @DisplayName("Should correctly convert ethereum block to address deltas")
    fun testRecordToDeltas() {
        val record = ConsumerRecord<PumpEvent, EthereumBlock>(EthereumFamilyChain.ETHEREUM.blockPumpTopic, 0, 0, PumpEvent.NEW_BLOCK, block)

        val deltas = EthereumBlockDeltaProcessor().recordToDeltas(record)
        Assertions.assertEquals(deltas, listOf(expectedDelta))
    }

    @Test
    @DisplayName("Should correctly convert ethereum block with dropped block event to address deltas")
    fun testRecordToDeltasDroppedBlock() {
        val record = ConsumerRecord<PumpEvent, EthereumBlock>(EthereumFamilyChain.ETHEREUM.blockPumpTopic, 0, 0, PumpEvent.DROPPED_BLOCK, block)

        val deltas = EthereumBlockDeltaProcessor().recordToDeltas(record)
        Assertions.assertEquals(deltas, listOf(expectedDroppedDelta))
    }

}

