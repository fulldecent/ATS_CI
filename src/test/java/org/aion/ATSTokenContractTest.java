package org.aion;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.abi.ABIDecoder;

import java.math.BigInteger;
import java.util.Arrays;
/*

package org.aion;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.core.util.LogSizeUtils;
import org.aion.avm.tooling.AvmRule;

import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.vm.api.interfaces.IExecutionLog;
import org.aion.vm.api.interfaces.ResultCode;
import org.junit.*;

import java.math.BigInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
*/
import org.junit.*;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;


public class ATSTokenContractTest {
    @Rule
    public AvmRule avmRule = new AvmRule(true);

    //default address with balance
    private Address deployer =  avmRule.getPreminedAccount();
    private Address contractAddress;

    private BigInteger nAmp = BigInteger.valueOf(1_000_000_000_000_000_000L);
    private String tokenName = "JENNIJUJU";
   // private String tokenNameNull = "";
    private String tokenSymbol = "J3N";
    //private int tokenGranularity = 1;
    private int tokenGranularity = 1;
    private byte[] tokenTotalSupply = BigInteger.valueOf(333_333_333_333_333_333L).multiply(nAmp).toByteArray();
    // byte[] tokenTotalSupply = BigInteger.ZERO.toByteArray();

    @Before
    public void deployDapp() {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] data = encoder.encodeOneString(tokenName)
                                .encodeOneString(tokenSymbol)
                                .encodeOneInteger(tokenGranularity)
                                .encodeOneByteArray(tokenTotalSupply)
                                .toBytes();
        byte[] contractData = avmRule.getDappBytes(ATSTokenContract.class, data, ATSTokenContractEvents.class);
        contractAddress = avmRule.deploy(deployer, BigInteger.ZERO, contractData).getDappAddress();
    }

    @Test
    public void testTheftDoesNotWorkWithoutWillsTechnique() {
        // Preconditions:
        // 1. Set token granularity to 1
        // Test case:
        // 1. Deployer sends some tokens to bandit
        // Expected outcome:
        // 1. isOperatorFor(Deployer, Bandit) is false

        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        Address bandit = avmRule.getRandomAddress(BigInteger.valueOf(3).multiply(nAmp));

        // 1. Deployer sends some tokens to bandit
        AvmRule.ResultWrapper result = avmRule.call(deployer,contractAddress,BigInteger.ZERO,
                encoder.encodeOneString("send")
                        .encodeOneAddress(bandit)
                        .encodeOneByteArray(BigInteger.valueOf(3).multiply(nAmp).toByteArray())
                        .encodeOneByteArray(null)
                        .toBytes());
        Assert.assertTrue(result.getReceiptStatus().isSuccess());

        // - isOperatorFor(Deployer, Bandit) is false
        result = avmRule.call(deployer,contractAddress,BigInteger.ZERO,
                encoder.encodeOneString("isOperatorFor")
                        .encodeOneAddress(bandit)
                        .encodeOneAddress(deployer)
                        .toBytes());
        Boolean res = (boolean) result.getDecodedReturnData();
        Assert.assertFalse(res);
    }


    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
public static String bytesToHex(byte[] bytes) {
char[] hexChars = new char[bytes.length * 2];
for (int j = 0; j < bytes.length; j++) {
        int v = bytes[j] & 0xFF;
        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
}
return new String(hexChars);
}


    @Test
    public void testTheftDoesNotWorkWithWillsTechnique() {
        // Test case:
        // 0. SET TOKEN GRANULARITY TO 1
        // 1. Deployer sends some tokens to bandit
        // 2. Bandit does special thing
        // Expected outcome:
        // - isOperatorFor(Deployer, Bandit) is false

        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        Address bandit = avmRule.getRandomAddress(BigInteger.valueOf(3).multiply(nAmp));

        // 1. Deployer sends some tokens to bandit
        AvmRule.ResultWrapper result = avmRule.call(deployer,contractAddress,BigInteger.ZERO,
                encoder.encodeOneString("send")
                        .encodeOneAddress(bandit)
                        .encodeOneByteArray(BigInteger.valueOf(3).multiply(nAmp).toByteArray())
                        .encodeOneByteArray(null)
                        .toBytes());
        Assert.assertTrue(result.getReceiptStatus().isSuccess());

        // 2. Bandit does special thing
        /*
        // I CAN"T FIGURE OUT WHY THIS DOES NOT WORK
        byte[] operatorAndTokenHolder = AionBuffer.allocate(Address.LENGTH * 2)
                                                  .putAddress(bandit).putAddress(deployer)
                                                  .getArray();
        byte[] magicAddressBytes = Blockchain.blake2b(operatorAndTokenHolder);
        Address magicAddress = new Address(magicAddressBytes);
        */
        // So instead I did this ugly hack:
        AvmRule.ResultWrapper result2 = avmRule.call(deployer,contractAddress,BigInteger.ZERO,
                encoder.encodeOneString("getMagicAddress")
                        .encodeOneAddress(bandit)
                        .toBytes());
                        
        byte[] resBytes = (byte[]) result2.getDecodedReturnData();
        Address magicAddress = new Address(resBytes);
        System.err.println(
                bytesToHex(
                        resBytes
                )
        );
        System.err.println(
                bytesToHex(
                        magicAddress.toByteArray()
                )
        );
        // END UGLY HACK

        AvmRule.ResultWrapper result3 = avmRule.call(bandit,contractAddress,BigInteger.ZERO,
                encoder.encodeOneString("send")
                        .encodeOneAddress(magicAddress)
//                        .encodeOneByteArray(magicAddressAsBytes) // NICE TRY
                        .encodeOneByteArray(BigInteger.valueOf(tokenGranularity).toByteArray())
                        .encodeOneByteArray(null)
                        .toBytes());
        Assert.assertTrue(result3.getReceiptStatus().isSuccess());

        // - isOperatorFor(Deployer, Bandit) is false
        result = avmRule.call(deployer,contractAddress,BigInteger.ZERO,
                encoder.encodeOneString("isOperatorFor")
                        .encodeOneAddress(bandit)
                        .encodeOneAddress(deployer)
                        .toBytes());
        Boolean res = (boolean) result.getDecodedReturnData();
        Assert.assertTrue(res);
    }

}
