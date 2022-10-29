import java.util.ArrayList;
import java.util.HashSet; 

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */ 
    UTXOPool currPool; 

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
       currPool = new UTXOPool(utxoPool); 
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        HashSet<UTXO> claimedUTXO = new HashSet<UTXO>(); 
        double inputSum = 0;  
        double outputSum = 0; 
        for (int i=0; i<tx.numInputs(); i++) {
            Transaction.Input input = inputs.get(i);
            UTXO currUTXO = new UTXO(input.prevTxHash, input.outputIndex); 
            if (claimedUTXO.contains(currUTXO)) {
                return false;
            }
            if (!currPool.contains(currUTXO)){
                return false; 
            }
            Transaction.Output output = currPool.getTxOutput(currUTXO); 
            inputSum += output.value; 
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature)) {
                return false; 
            }
            claimedUTXO.add(currUTXO); 
        }
        for (Transaction.Output output: outputs) {
            if (output.value < 0) {
                return false; 
            }
            outputSum += output.value; 
        }
        if (inputSum < outputSum) {
            return false; 
        }
        return true; 
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> validTxs = new ArrayList<Transaction>();
        for (Transaction tx: possibleTxs) {
            if (isValidTx(tx)) {
                validTxs.add(tx);
                for (Transaction.Input input : tx.getInputs()) {
                    currPool.removeUTXO(new UTXO(input.prevTxHash, input.outputIndex));
                }
            }
        }
        return validTxs.stream().toArray(Transaction[]::new);
    }
}
