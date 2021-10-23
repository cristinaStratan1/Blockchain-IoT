import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public final class AgreementRepositoryTest {

    @Nested
    class InvokeQueryAgreementTransaction {

        @Test
        public void whenAgreementExists() {
            AgreementRepository contract = new AgreementRepository();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("ARG000"))
                    .thenReturn("{\"party1\":\"Vendor\",\"party2\":\"Gateway\",\"status\":\"issued\"}");

            Agreement agreement = contract.getAgreement(ctx, "ARG000");

            assertThat(agreement.getParty1())
                    .isEqualTo("Vendor");
            assertThat(agreement.getParty2())
                    .isEqualTo("Gateway");
            assertThat(agreement.getStatus())
                    .isEqualTo("issued");
        }

        @Test
        public void whenAgreementDoesNotExist() {
            AgreementRepository contract = new AgreementRepository();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("ARG000")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.getAgreement(ctx, "ARG000");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Agreement ARG000 does not exist");
        }

        @Test
        public void uploadFileToIPFS(){
            try {
                NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(new File("/home/cris/Downloads/hlf2-network/chaincode/java/agreements/update.txt"));
                IPFS ipfs = new IPFS("localhost", 5001);
                MerkleNode response = ipfs.add(file).get(0);
                System.out.println("Hash (base 58): " + response.hash.toBase58());
            } catch (IOException ex) {
                throw new RuntimeException("Error whilst communicating with the IPFS node", ex);
            }
        }

        @Test
        public void sendIPFS() throws MqttException {
            MqttClient BlockChain = new MqttClient("tcp://broker.emqx.io:1883", "firmware_sender");
            BlockChain.connect();
            MqttMessage message = new MqttMessage();
            message.setPayload(("new_update CID= QmZTR5bcpQD7cFgTorqxZDYaew1Wqgfbd2ud9QqGPAkK2V").getBytes());
            BlockChain.publish("firmwareUpdate", message);
        }
    }


}