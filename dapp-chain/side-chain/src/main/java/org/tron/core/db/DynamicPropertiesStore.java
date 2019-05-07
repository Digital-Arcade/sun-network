package org.tron.core.db;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.capsule.BytesCapsule;
import org.tron.core.config.Parameter;
import org.tron.core.config.Parameter.ChainConstant;
import org.tron.core.config.args.Args;

@Slf4j(topic = "DB")
@Component
public class DynamicPropertiesStore extends TronStoreWithRevoking<BytesCapsule> {

  private static final byte[] LATEST_BLOCK_HEADER_TIMESTAMP = "latest_block_header_timestamp"
      .getBytes();
  private static final byte[] LATEST_BLOCK_HEADER_NUMBER = "latest_block_header_number".getBytes();
  private static final byte[] LATEST_BLOCK_HEADER_HASH = "latest_block_header_hash".getBytes();
  private static final byte[] STATE_FLAG = "state_flag"
      .getBytes(); // 1 : is maintenance, 0 : is not maintenance
  private static final byte[] LATEST_SOLIDIFIED_BLOCK_NUM = "LATEST_SOLIDIFIED_BLOCK_NUM"
      .getBytes();

  private static final byte[] LATEST_PROPOSAL_NUM = "LATEST_PROPOSAL_NUM".getBytes();

  private static final byte[] LATEST_EXCHANGE_NUM = "LATEST_EXCHANGE_NUM".getBytes();

  private static final byte[] BLOCK_FILLED_SLOTS = "BLOCK_FILLED_SLOTS".getBytes();

  private static final byte[] BLOCK_FILLED_SLOTS_INDEX = "BLOCK_FILLED_SLOTS_INDEX".getBytes();

  private static final byte[] NEXT_MAINTENANCE_TIME = "NEXT_MAINTENANCE_TIME".getBytes();

  private static final byte[] MAX_FROZEN_TIME = "MAX_FROZEN_TIME".getBytes();

  private static final byte[] MIN_FROZEN_TIME = "MIN_FROZEN_TIME".getBytes();

  private static final byte[] MAX_FROZEN_SUPPLY_NUMBER = "MAX_FROZEN_SUPPLY_NUMBER".getBytes();

  private static final byte[] MAX_FROZEN_SUPPLY_TIME = "MAX_FROZEN_SUPPLY_TIME".getBytes();

  private static final byte[] MIN_FROZEN_SUPPLY_TIME = "MIN_FROZEN_SUPPLY_TIME".getBytes();

  private static final byte[] WITNESS_ALLOWANCE_FROZEN_TIME = "WITNESS_ALLOWANCE_FROZEN_TIME"
      .getBytes();

  private static final byte[] MAINTENANCE_TIME_INTERVAL = "MAINTENANCE_TIME_INTERVAL".getBytes();

  private static final byte[] ACCOUNT_UPGRADE_COST = "ACCOUNT_UPGRADE_COST".getBytes();

  private static final byte[] WITNESS_PAY_PER_BLOCK = "WITNESS_PAY_PER_BLOCK".getBytes();

  private static final byte[] WITNESS_STANDBY_ALLOWANCE = "WITNESS_STANDBY_ALLOWANCE".getBytes();

  private static class DynamicResourceProperties {

    private static final byte[] ONE_DAY_NET_LIMIT = "ONE_DAY_NET_LIMIT".getBytes();
    //public free bandwidth
    private static final byte[] PUBLIC_NET_USAGE = "PUBLIC_NET_USAGE".getBytes();
    //fixed
    private static final byte[] PUBLIC_NET_LIMIT = "PUBLIC_NET_LIMIT".getBytes();
    private static final byte[] PUBLIC_NET_TIME = "PUBLIC_NET_TIME".getBytes();
    private static final byte[] FREE_NET_LIMIT = "FREE_NET_LIMIT".getBytes();
    private static final byte[] TOTAL_NET_WEIGHT = "TOTAL_NET_WEIGHT".getBytes();
    //ONE_DAY_NET_LIMIT - PUBLIC_NET_LIMIT，current TOTAL_NET_LIMIT
    private static final byte[] TOTAL_NET_LIMIT = "TOTAL_NET_LIMIT".getBytes();
    private static final byte[] TOTAL_ENERGY_TARGET_LIMIT = "TOTAL_ENERGY_TARGET_LIMIT".getBytes();
    private static final byte[] TOTAL_ENERGY_CURRENT_LIMIT = "TOTAL_ENERGY_CURRENT_LIMIT"
        .getBytes();
    private static final byte[] TOTAL_ENERGY_AVERAGE_USAGE = "TOTAL_ENERGY_AVERAGE_USAGE"
        .getBytes();
    private static final byte[] TOTAL_ENERGY_AVERAGE_TIME = "TOTAL_ENERGY_AVERAGE_TIME".getBytes();
    private static final byte[] TOTAL_ENERGY_WEIGHT = "TOTAL_ENERGY_WEIGHT".getBytes();
    private static final byte[] TOTAL_ENERGY_LIMIT = "TOTAL_ENERGY_LIMIT".getBytes();
    private static final byte[] BLOCK_ENERGY_USAGE = "BLOCK_ENERGY_USAGE".getBytes();
  }

  private static final byte[] ENERGY_FEE = "ENERGY_FEE".getBytes();

  private static final byte[] MAX_CPU_TIME_OF_ONE_TX = "MAX_CPU_TIME_OF_ONE_TX".getBytes();

  //abandon
  private static final byte[] CREATE_ACCOUNT_FEE = "CREATE_ACCOUNT_FEE".getBytes();



  private static final byte[] CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT
      = "CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT".getBytes();

  private static final byte[] CREATE_NEW_ACCOUNT_BANDWIDTH_RATE = "CREATE_NEW_ACCOUNT_BANDWIDTH_RATE"
      .getBytes();

  private static final byte[] TRANSACTION_FEE = "TRANSACTION_FEE".getBytes(); // 1 byte

  private static final byte[] TRANSACTION_SUNTOKEN_FEE = "TRANSACTION_SUNTOKEN_FEE".getBytes(); // 1 byte : 100 micro suntoken

  private static final byte[] ASSET_ISSUE_FEE = "ASSET_ISSUE_FEE".getBytes();

  private static final byte[] UPDATE_ACCOUNT_PERMISSION_FEE = "UPDATE_ACCOUNT_PERMISSION_FEE"
      .getBytes();

  private static final byte[] MULTI_SIGN_FEE = "MULTI_SIGN_FEE"
      .getBytes();


  private static final byte[] EXCHANGE_CREATE_FEE = "EXCHANGE_CREATE_FEE".getBytes();

  private static final byte[] EXCHANGE_BALANCE_LIMIT = "EXCHANGE_BALANCE_LIMIT".getBytes();

  private static final byte[] TOTAL_TRANSACTION_COST = "TOTAL_TRANSACTION_COST".getBytes();

  private static final byte[] TOTAL_CREATE_ACCOUNT_COST = "TOTAL_CREATE_ACCOUNT_COST".getBytes();

  private static final byte[] TOTAL_CREATE_WITNESS_COST = "TOTAL_CREATE_WITNESS_FEE".getBytes();

  private static final byte[] TOTAL_STORAGE_POOL = "TOTAL_STORAGE_POOL".getBytes();

  private static final byte[] TOTAL_STORAGE_TAX = "TOTAL_STORAGE_TAX".getBytes();

  private static final byte[] TOTAL_STORAGE_RESERVED = "TOTAL_STORAGE_RESERVED".getBytes();

  private static final byte[] STORAGE_EXCHANGE_TAX_RATE = "STORAGE_EXCHANGE_TAX_RATE".getBytes();

  private static final byte[] FORK_CONTROLLER = "FORK_CONTROLLER".getBytes();
  private static final String FORK_PREFIX = "FORK_VERSION_";

  //This value is only allowed to be 0, 1, -1
  private static final byte[] REMOVE_THE_POWER_OF_THE_GR = "REMOVE_THE_POWER_OF_THE_GR".getBytes();

  //This value is only allowed to be 0, 1, -1
  private static final byte[] ALLOW_DELEGATE_RESOURCE = "ALLOW_DELEGATE_RESOURCE".getBytes();

  //This value is only allowed to be 0, 1, -1
  private static final byte[] ALLOW_ADAPTIVE_ENERGY = "ALLOW_ADAPTIVE_ENERGY".getBytes();

  //This value is only allowed to be 0, 1, -1
  private static final byte[] ALLOW_UPDATE_ACCOUNT_NAME = "ALLOW_UPDATE_ACCOUNT_NAME".getBytes();

  //This value is only allowed to be 0, 1, -1
  private static final byte[] ALLOW_SAME_TOKEN_NAME = " ALLOW_SAME_TOKEN_NAME".getBytes();

  //If the parameter is larger than 0, the contract is allowed to be created.
  private static final byte[] ALLOW_CREATION_OF_CONTRACTS = "ALLOW_CREATION_OF_CONTRACTS"
      .getBytes();

  //Used only for multi sign
  private static final byte[] TOTAL_SIGN_NUM = "TOTAL_SIGN_NUM".getBytes();

  //Used only for multi sign, once，value is {0,1}
  private static final byte[] ALLOW_MULTI_SIGN = "ALLOW_MULTI_SIGN".getBytes();

  //token id,Incremental，The initial value is 1000000
  private static final byte[] TOKEN_ID_NUM = "TOKEN_ID_NUM".getBytes();

  //Used only for token updates, once，value is {0,1}
  private static final byte[] TOKEN_UPDATE_DONE = "TOKEN_UPDATE_DONE".getBytes();

  //This value is only allowed to be 0, 1, -1
  private static final byte[] ALLOW_TVM_TRANSFER_TRC10 = "ALLOW_TVM_TRANSFER_TRC10".getBytes();

  private static final byte[] AVAILABLE_CONTRACT_TYPE = "AVAILABLE_CONTRACT_TYPE".getBytes();
  private static final byte[] ACTIVE_DEFAULT_OPERATIONS = "ACTIVE_DEFAULT_OPERATIONS".getBytes();

  /*
   * Side-chain parameters.
   */

  // gateway list
  private static final byte[] GATEWAY_ADDRESS_LIST = "GATEWAY_ADDRESS_LIST".getBytes();

  // main chain gateway list
  private static final byte[] MAINCHAIN_GATEWAY_ADDRESS_LIST = "MAINCHAIN_GATEWAY_ADDRESS_LIST".getBytes();

  // basic value for energy: byte, also means energy: bandwidth (10:1)
  private static final byte[] TRANSACTION_ENERGY_BYTE_RATE = "TRANSACTION_ENERGY_BYTE_RATE".getBytes();

  // basic value for suntoken energy: byte, also means energy: bandwidth (10:1)
  private static final byte[] TRANSACTION_SUNTOKEN_ENERGY_BYTE_RATE = "TRANSACTION_SUNTOKEN_ENERGY_BYTE_RATE".getBytes();

  // Define the energy/byte rate, when create an account using frozen energy.
  private static final byte[] CREATE_NEW_ACCOUNT_ENERGY_BYTE_RATE = "CREATE_NEW_ACCOUNT_ENERGY_BYTE_RATE".getBytes();

  // Define the energy/byte rate, when create an account using frozen energy.
  private static final byte[] CREATE_NEW_ACCOUNT_SUNTOKEN_ENERGY_BYTE_RATE = "CREATE_NEW_ACCOUNT_SUNTOKEN_ENERGY_BYTE_RATE".getBytes();

  // switch on to kick off energy charging
  private static final byte[] ENERGY_CHARGING_SWITCH = "ENERGY_CHARGING_SWITCH".getBytes();

  // side chain charging type
  private static final byte[] SIDECHAIN_CHARGING_TYPE = "SIDECHAIN_CHARGING_TYPE".getBytes();

  // side chain charging bandwidth
  private static final byte[] SIDECHAIN_CHARGING_BANDWIDTH = "SIDECHAIN_CHARGING_BANDWIDTH".getBytes();

  // CREATE_ACCOUNT_FEE              0.1 SUN_TOKEN
  private static final byte[] CREATE_ACCOUNT_SUNTOKEN_FEE = "CREATE_ACCOUNT_SUNTOKEN_FEE".getBytes();

  // CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT
  private static final byte[] CREATE_NEW_ACCOUNT_TOKEN_FEE_IN_SYSTEM_CONTRACT = "CREATE_NEW_ACCOUNT_TOKEN_FEE_IN_SYSTEM_CONTRACT".getBytes();

  // UPDATE_ACCOUNT_PERMISSION_FEE
  private static final byte[] UPDATE_ACCOUNT_PERMISSION_TOKEN_FEE = "UPDATE_ACCOUNT_PERMISSION_TOKEN_FEE"
      .getBytes();

  // MULTI_SIGN_FEE
  private static final byte[] MULTI_SIGN_TOKEN_FEE = "MULTI_SIGN_TOKEN_FEE"
      .getBytes();

  // ACCOUNT_UPGRADE_COST
  private static final byte[] ACCOUNT_UPGRADE_TOKEN_COST = "ACCOUNT_UPGRADE_TOKEN_COST".getBytes();



  /**
   *   Used when calculating available energy limit. Similar to ENERGY_FEE in mainchain.
   *   100 micro sun token per 1 energy for its initial value
   */
  private static final byte[] ENERGY_TOKEN_FEE = "ENERGY_TOKEN_FEE".getBytes();

  @Autowired
  private DynamicPropertiesStore(@Value("properties") String dbName) {
    super(dbName);

    try {
      this.getTotalSignNum();
    } catch (IllegalArgumentException e) {
      this.saveTotalSignNum(5);
    }

    try {
      this.getAllowMultiSign();
    } catch (IllegalArgumentException e) {
      this.saveAllowMultiSign(Args.getInstance().getAllowMultiSign());
    }

    try {
      this.getLatestBlockHeaderTimestamp();
    } catch (IllegalArgumentException e) {
      this.saveLatestBlockHeaderTimestamp(0);
    }

    try {
      this.getLatestBlockHeaderNumber();
    } catch (IllegalArgumentException e) {
      this.saveLatestBlockHeaderNumber(0);
    }

    try {
      this.getLatestBlockHeaderHash();
    } catch (IllegalArgumentException e) {
      this.saveLatestBlockHeaderHash(ByteString.copyFrom(ByteArray.fromHexString("00")));
    }

    try {
      this.getStateFlag();
    } catch (IllegalArgumentException e) {
      this.saveStateFlag(0);
    }

    try {
      this.getLatestSolidifiedBlockNum();
    } catch (IllegalArgumentException e) {
      this.saveLatestSolidifiedBlockNum(0);
    }

    try {
      this.getLatestProposalNum();
    } catch (IllegalArgumentException e) {
      this.saveLatestProposalNum(0);
    }

    try {
      this.getLatestExchangeNum();
    } catch (IllegalArgumentException e) {
      this.saveLatestExchangeNum(0);
    }

    try {
      this.getBlockFilledSlotsIndex();
    } catch (IllegalArgumentException e) {
      this.saveBlockFilledSlotsIndex(0);
    }

    try {
      this.getTokenIdNum();
    } catch (IllegalArgumentException e) {
      this.saveTokenIdNum(1000000L);
    }

    try {
      this.getTokenUpdateDone();
    } catch (IllegalArgumentException e) {
      this.saveTokenUpdateDone(0);
    }

    try {
      this.getMaxFrozenTime();
    } catch (IllegalArgumentException e) {
      this.saveMaxFrozenTime(3);
    }

    try {
      this.getMinFrozenTime();
    } catch (IllegalArgumentException e) {
      this.saveMinFrozenTime(3);
    }

    try {
      this.getMaxFrozenSupplyNumber();
    } catch (IllegalArgumentException e) {
      this.saveMaxFrozenSupplyNumber(10);
    }

    try {
      this.getMaxFrozenSupplyTime();
    } catch (IllegalArgumentException e) {
      this.saveMaxFrozenSupplyTime(3652);
    }

    try {
      this.getMinFrozenSupplyTime();
    } catch (IllegalArgumentException e) {
      this.saveMinFrozenSupplyTime(1);
    }

    try {
      this.getWitnessAllowanceFrozenTime();
    } catch (IllegalArgumentException e) {
      this.saveWitnessAllowanceFrozenTime(1);
    }

    try {
      this.getWitnessPayPerBlock();
    } catch (IllegalArgumentException e) {
      this.saveWitnessPayPerBlock(0); // side chain equals to 0 by default
    }

    try {
      this.getWitnessStandbyAllowance();
    } catch (IllegalArgumentException e) {
      this.saveWitnessStandbyAllowance(115_200_000_000L);
    }

    try {
      this.getMaintenanceTimeInterval();
    } catch (IllegalArgumentException e) {
      this.saveMaintenanceTimeInterval(Args.getInstance().getMaintenanceTimeInterval()); // 6 hours
    }

    try {
      this.getAccountUpgradeCost();
    } catch (IllegalArgumentException e) {
      this.saveAccountUpgradeCost(9_999_000_000L);
    }

    try {
      this.getAccountUpgradeTokenCost();
    } catch (IllegalArgumentException e) {
      this.saveAccountUpgradeTokenCost(9_999_000_000L);  //side chain
    }

    try {
      this.getPublicNetUsage();
    } catch (IllegalArgumentException e) {
      this.savePublicNetUsage(0L);
    }

    try {
      this.getOneDayNetLimit();
    } catch (IllegalArgumentException e) {
      this.saveOneDayNetLimit(57_600_000_000L);
    }

    try {
      this.getPublicNetLimit();
    } catch (IllegalArgumentException e) {
      this.savePublicNetLimit(14_400_000_000L);
    }

    try {
      this.getPublicNetTime();
    } catch (IllegalArgumentException e) {
      this.savePublicNetTime(0L);
    }

    try {
      this.getFreeNetLimit();
    } catch (IllegalArgumentException e) {
      this.saveFreeNetLimit(5000L);
    }

    try {
      this.getTotalNetWeight();
    } catch (IllegalArgumentException e) {
      this.saveTotalNetWeight(0L);
    }

    try {
      this.getTotalNetLimit();
    } catch (IllegalArgumentException e) {
      this.saveTotalNetLimit(43_200_000_000L);
    }

    try {
      this.getTotalEnergyWeight();
    } catch (IllegalArgumentException e) {
      this.saveTotalEnergyWeight(0L);
    }

    try {
      this.getAllowAdaptiveEnergy();
    } catch (IllegalArgumentException e) {
      this.saveAllowAdaptiveEnergy(Args.getInstance().getAllowAdaptiveEnergy());
    }

    try {
      this.getTotalEnergyLimit();
    } catch (IllegalArgumentException e) {
      this.saveTotalEnergyLimit(50_000_000_000L);
    }

    try {
      this.getEnergyFee();
    } catch (IllegalArgumentException e) {
      this.saveEnergyFee(100L);// 100 sun per energy
    }

    try {
      this.getEnergyTokenFee();
    } catch (IllegalArgumentException e) {
      this.saveEnergyTokenFee(100L);// 100 micro sun-token per energy
    }

    try {
      this.getMaxCpuTimeOfOneTx();
    } catch (IllegalArgumentException e) {
      this.saveMaxCpuTimeOfOneTx(50L);
    }

    try {
      this.getCreateAccountFee();
    } catch (IllegalArgumentException e) {
      this.saveCreateAccountFee(100_000L); // 0.1TRX
    }

    try {
      this.getCreateAccountSunTokenFee();
    } catch (IllegalArgumentException e) {
      this.saveCreateAccountSunTokenFee(100_000L); // 0.1SunToken
    }

    try {
      this.getCreateNewAccountFeeInSystemContract();
    } catch (IllegalArgumentException e) {
      this.saveCreateNewAccountFeeInSystemContract(0L); //changed by committee later
    }

    try {
      this.getCreateNewAccountTokenFeeInSystemContract();
    } catch (IllegalArgumentException e) {
      this.saveCreateNewAccountTokenFeeInSystemContract(0L); // side chain
    }

    try {
      this.getCreateNewAccountBandwidthRate();
    } catch (IllegalArgumentException e) {
      this.saveCreateNewAccountBandwidthRate(1L); //changed by committee later
    }

    try {
      this.getCreateNewAccountEnergyRate();
    } catch (IllegalArgumentException e) {
      this.saveCreateNewAccountEnergyRate(10);
      // 10 byte(bandwidth) == 1 energy
    }

    try {
      this.getTransactionFee();
    } catch (IllegalArgumentException e) {
      this.saveTransactionFee(10L); // 10sun/byte
    }

    try {
      this.getTransactionEnergyByteRate();
    } catch (IllegalArgumentException e) {
      this.saveTransactionEnergyByteRate(10);
      // 10 byte(bandwidth) == 1 energy
    }

    try {
      this.getCreateNewAccountSunTokenEnergyRate();
    } catch (IllegalArgumentException e) {
      this.saveCreateNewAccountSunTokenEnergyRate(10);
      // 10 byte(bandwidth) == 1 energy
    }

    try {
      this.getTransactionSunTokenFee();
    } catch (IllegalArgumentException e) {
      this.saveTransactionSunTokenFee(100L); // 100micro suntoken/byte
    }

    try {
      this.getTransactionSunTokenEnergyByteRate();
    } catch (IllegalArgumentException e) {
      this.saveTransactionSunTokenEnergyByteRate(10);
      // 10 byte(bandwidth) == 1 energy
    }

    try {
      this.getAssetIssueFee();
    } catch (IllegalArgumentException e) {
      this.saveAssetIssueFee(1024000000L);
    }

    try {
      this.getUpdateAccountPermissionFee();
    } catch (IllegalArgumentException e) {
      this.saveUpdateAccountPermissionFee(100000000L);
    }

    try {
      this.getUpdateAccountPermissionTokenFee();
    } catch (IllegalArgumentException e) {
      this.saveUpdateAccountPermissionTokenFee(100000000L);  //side chain
    }

    try {
      this.getMultiSignFee();
    } catch (IllegalArgumentException e) {
      this.saveMultiSignFee(1000000L);
    }

    try {
      this.getMultiSignTokenFee();
    } catch (IllegalArgumentException e) {
      this.saveMultiSignTokenFee(1000000L);  //side chain
    }


    try {
      this.getExchangeCreateFee();
    } catch (IllegalArgumentException e) {
      this.saveExchangeCreateFee(1024000000L);
    }

    try {
      this.getExchangeBalanceLimit();
    } catch (IllegalArgumentException e) {
      this.saveExchangeBalanceLimit(1_000_000_000_000_000L);
    }

    try {
      this.getTotalTransactionCost();
    } catch (IllegalArgumentException e) {
      this.saveTotalTransactionCost(0L);
    }

    try {
      this.getTotalCreateWitnessCost();
    } catch (IllegalArgumentException e) {
      this.saveTotalCreateWitnessFee(0L);
    }

    try {
      this.getTotalCreateAccountCost();
    } catch (IllegalArgumentException e) {
      this.saveTotalCreateAccountFee(0L);
    }

    try {
      this.getTotalStoragePool();
    } catch (IllegalArgumentException e) {
      this.saveTotalStoragePool(100_000_000_000_000L);
    }

    try {
      this.getTotalStorageTax();
    } catch (IllegalArgumentException e) {
      this.saveTotalStorageTax(0);
    }

    try {
      this.getTotalStorageReserved();
    } catch (IllegalArgumentException e) {
      this.saveTotalStorageReserved(128L * 1024 * 1024 * 1024); // 137438953472 bytes
    }

    try {
      this.getStorageExchangeTaxRate();
    } catch (IllegalArgumentException e) {
      this.saveStorageExchangeTaxRate(10);
    }

    try {
      this.getRemoveThePowerOfTheGr();
    } catch (IllegalArgumentException e) {
      this.saveRemoveThePowerOfTheGr(0);
    }

    try {
      this.getAllowDelegateResource();
    } catch (IllegalArgumentException e) {
      this.saveAllowDelegateResource(Args.getInstance().getAllowDelegateResource());
    }

    try {
      this.getAllowTvmTransferTrc10();
    } catch (IllegalArgumentException e) {
      this.saveAllowTvmTransferTrc10(Args.getInstance().getAllowTvmTransferTrc10());
    }

    try {
      this.getAvailableContractType();
    } catch (IllegalArgumentException e) {
      String contractType = "7fff1fc0037e0000000000000000000000000000000000000000000000000000";
      byte[] bytes = ByteArray.fromHexString(contractType);
      this.saveAvailableContractType(bytes);
    }

    try {
      this.getActiveDefaultOperations();
    } catch (IllegalArgumentException e) {
      String contractType = "7fff1fc0033e0000000000000000000000000000000000000000000000000000";
      byte[] bytes = ByteArray.fromHexString(contractType);
      this.saveActiveDefaultOperations(bytes);
    }

    try {
      this.getBlockFilledSlots();
    } catch (IllegalArgumentException e) {
      int[] blockFilledSlots = new int[getBlockFilledSlotsNumber()];
      Arrays.fill(blockFilledSlots, 1);
      this.saveBlockFilledSlots(blockFilledSlots);
    }

    try {
      this.getNextMaintenanceTime();
    } catch (IllegalArgumentException e) {
      this.saveNextMaintenanceTime(
          Long.parseLong(Args.getInstance().getGenesisBlock().getTimestamp()));
    }

    try {
      this.getTotalEnergyCurrentLimit();
    } catch (IllegalArgumentException e) {
      this.saveTotalEnergyCurrentLimit(getTotalEnergyLimit());
    }

    try {
      this.getTotalEnergyTargetLimit();
    } catch (IllegalArgumentException e) {
      this.saveTotalEnergyTargetLimit(getTotalEnergyLimit() / 14400);
    }

    try {
      this.getTotalEnergyAverageUsage();
    } catch (IllegalArgumentException e) {
      this.saveTotalEnergyAverageUsage(0);
    }

    try {
      this.getTotalEnergyAverageTime();
    } catch (IllegalArgumentException e) {
      this.saveTotalEnergyAverageTime(0);
    }

    try {
      this.getBlockEnergyUsage();
    } catch (IllegalArgumentException e) {
      this.saveBlockEnergyUsage(0);
    }

    try {
      this.getGateWayList();
    } catch (IllegalArgumentException e) {
      this.saveGateWayList(Args.getInstance().getGatewayList());
    }

    try {
      this.getMainChainGateWayList();
    } catch (IllegalArgumentException e) {
      this.saveMainChainGateWayList(Args.getInstance().getMainChainGateWayList());
    }

    try {
      this.getEnergyChargingSwitch();
    } catch (IllegalArgumentException e) {
      this.saveEnergyChargingSwitch(Args.getInstance().getEnergyChargingSwitchOn());
    }

    try {
      this.getSideChainChargingType();
    } catch (IllegalArgumentException e) {
      this.saveSideChainChargingType(Args.getInstance().getSideChainChargingType());
    }

    try {
      this.getSideChainChargingBandwidth();
    } catch (IllegalArgumentException e) {
      this.saveSideChainChargingBandwidth(Args.getInstance().getSideChainChargingBandwidth());
    }
  }

  public String intArrayToString(int[] a) {
    StringBuilder sb = new StringBuilder();
    for (int i : a) {
      sb.append(i);
    }
    return sb.toString();
  }

  public int[] stringToIntArray(String s) {
    int length = s.length();
    int[] result = new int[length];
    for (int i = 0; i < length; ++i) {
      result[i] = Integer.parseInt(s.substring(i, i + 1));
    }
    return result;
  }

  /**
   *   Side-chain parameters
   *
   */


  public int getTransactionEnergyByteRate() {
    return Optional.ofNullable(getUnchecked(TRANSACTION_ENERGY_BYTE_RATE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TRANSACTION_ENERGY_BYTE_RATE"));
  }

  public void saveTransactionEnergyByteRate(int num) {
    this.put(TRANSACTION_ENERGY_BYTE_RATE,
        new BytesCapsule(ByteArray.fromInt(num)));
  }

  public int getTransactionSunTokenEnergyByteRate() {
    return Optional.ofNullable(getUnchecked(TRANSACTION_SUNTOKEN_ENERGY_BYTE_RATE))
            .map(BytesCapsule::getData)
            .map(ByteArray::toInt)
            .orElseThrow(
                    () -> new IllegalArgumentException("not found TRANSACTION_SUNTOKEN_ENERGY_BYTE_RATE"));
  }

  public void saveTransactionSunTokenEnergyByteRate(int num) {
    this.put(TRANSACTION_SUNTOKEN_ENERGY_BYTE_RATE,
            new BytesCapsule(ByteArray.fromInt(num)));
  }

  public int getTransactionEnergyByteRate(int chargingType) {
    if(chargingType == 0) {
      return getTransactionEnergyByteRate();
    }

    return getTransactionSunTokenEnergyByteRate();
  }

  public List<byte[]> getGateWayList() {
    return Optional.ofNullable(getUnchecked(GATEWAY_ADDRESS_LIST))
        .map(BytesCapsule::getData)
        .map(ByteArray::toByte21List)
        .orElseThrow(
            () -> new IllegalArgumentException("not found GATEWAY_ADDRESS_LIST"));
  }

  public void saveGateWayList(List<byte[]> gateWayList) {
    this.put(GATEWAY_ADDRESS_LIST,
        new BytesCapsule(ByteArray.fromBytes21List(gateWayList)));
  }

  public List<byte[]> getMainChainGateWayList() {
    return Optional.ofNullable(getUnchecked(MAINCHAIN_GATEWAY_ADDRESS_LIST))
        .map(BytesCapsule::getData)
        .map(ByteArray::toByte21List)
        .orElseThrow(
            () -> new IllegalArgumentException("not found MAINCHAIN_GATEWAY_ADDRESS_LIST"));
  }

  public void saveMainChainGateWayList(List<byte[]> gateWayList) {
    this.put(MAINCHAIN_GATEWAY_ADDRESS_LIST,
        new BytesCapsule(ByteArray.fromBytes21List(gateWayList)));
  }

  public void addToGateWayList(byte[] gateWayContractAddress) {
    List<byte[]> list =  Optional.ofNullable(getUnchecked(GATEWAY_ADDRESS_LIST))
        .map(BytesCapsule::getData)
        .map(ByteArray::toByte21List)
        .orElseThrow(
            () -> new IllegalArgumentException("not found GATEWAY_ADDRESS_LIST"));
    if(gateWayContractAddress.length != 21) {
      throw new IllegalArgumentException("new added gate way address should be 21 bytes");
    }
    list.add(gateWayContractAddress);
    this.put(GATEWAY_ADDRESS_LIST,
        new BytesCapsule(ByteArray.fromBytes21List(list)));
  }

  public int getEnergyChargingSwitch(){
    return Optional.ofNullable(getUnchecked(ENERGY_CHARGING_SWITCH))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ENERGY_CHARGING_SWITCH"));
  }

  public void saveEnergyChargingSwitch(long num) {
    this.put(ENERGY_CHARGING_SWITCH,
        new BytesCapsule(ByteArray.fromLong(num)));
  }

  public int getSideChainChargingType(){
    return Optional.ofNullable(getUnchecked(SIDECHAIN_CHARGING_TYPE))
            .map(BytesCapsule::getData)
            .map(ByteArray::toInt)
            .orElseThrow(
                    () -> new IllegalArgumentException("not found SIDECHAIN_CHARGING_TYPE"));
  }

  public void saveSideChainChargingType(long num) {
    this.put(SIDECHAIN_CHARGING_TYPE,
            new BytesCapsule(ByteArray.fromLong(num)));
  }

  public int getSideChainChargingBandwidth(){
    return Optional.ofNullable(getUnchecked(SIDECHAIN_CHARGING_BANDWIDTH))
            .map(BytesCapsule::getData)
            .map(ByteArray::toInt)
            .orElseThrow(
                    () -> new IllegalArgumentException("not found SIDECHAIN_CHARGING_BANDWIDTH"));
  }

  public void saveSideChainChargingBandwidth(long num) {
    this.put(SIDECHAIN_CHARGING_BANDWIDTH,
            new BytesCapsule(ByteArray.fromLong(num)));
  }



  /**
   *   SideChain parameter end
   */


  public void saveTokenIdNum(long num) {
    this.put(TOKEN_ID_NUM,
        new BytesCapsule(ByteArray.fromLong(num)));
  }

  public long getTokenIdNum() {
    return Optional.ofNullable(getUnchecked(TOKEN_ID_NUM))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOKEN_ID_NUM"));
  }

  public void saveTokenUpdateDone(long num) {
    this.put(TOKEN_UPDATE_DONE,
        new BytesCapsule(ByteArray.fromLong(num)));
  }

  public long getTokenUpdateDone() {
    return Optional.ofNullable(getUnchecked(TOKEN_UPDATE_DONE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOKEN_UPDATE_DONE"));
  }


  public void saveBlockFilledSlotsIndex(int blockFilledSlotsIndex) {
    logger.debug("blockFilledSlotsIndex:" + blockFilledSlotsIndex);
    this.put(BLOCK_FILLED_SLOTS_INDEX,
        new BytesCapsule(ByteArray.fromInt(blockFilledSlotsIndex)));
  }

  public int getBlockFilledSlotsIndex() {
    return Optional.ofNullable(getUnchecked(BLOCK_FILLED_SLOTS_INDEX))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(
            () -> new IllegalArgumentException("not found BLOCK_FILLED_SLOTS_INDEX"));
  }

  public void saveMaxFrozenTime(int maxFrozenTime) {
    logger.debug("MAX_FROZEN_NUMBER:" + maxFrozenTime);
    this.put(MAX_FROZEN_TIME,
        new BytesCapsule(ByteArray.fromInt(maxFrozenTime)));
  }

  public int getMaxFrozenTime() {
    return Optional.ofNullable(getUnchecked(MAX_FROZEN_TIME))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(
            () -> new IllegalArgumentException("not found MAX_FROZEN_TIME"));
  }

  public void saveMinFrozenTime(int minFrozenTime) {
    logger.debug("MIN_FROZEN_NUMBER:" + minFrozenTime);
    this.put(MIN_FROZEN_TIME,
        new BytesCapsule(ByteArray.fromInt(minFrozenTime)));
  }

  public int getMinFrozenTime() {
    return Optional.ofNullable(getUnchecked(MIN_FROZEN_TIME))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(
            () -> new IllegalArgumentException("not found MIN_FROZEN_TIME"));
  }

  public void saveMaxFrozenSupplyNumber(int maxFrozenSupplyNumber) {
    logger.debug("MAX_FROZEN_SUPPLY_NUMBER:" + maxFrozenSupplyNumber);
    this.put(MAX_FROZEN_SUPPLY_NUMBER,
        new BytesCapsule(ByteArray.fromInt(maxFrozenSupplyNumber)));
  }

  public int getMaxFrozenSupplyNumber() {
    return Optional.ofNullable(getUnchecked(MAX_FROZEN_SUPPLY_NUMBER))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(
            () -> new IllegalArgumentException("not found MAX_FROZEN_SUPPLY_NUMBER"));
  }

  public void saveMaxFrozenSupplyTime(int maxFrozenSupplyTime) {
    logger.debug("MAX_FROZEN_SUPPLY_NUMBER:" + maxFrozenSupplyTime);
    this.put(MAX_FROZEN_SUPPLY_TIME,
        new BytesCapsule(ByteArray.fromInt(maxFrozenSupplyTime)));
  }

  public int getMaxFrozenSupplyTime() {
    return Optional.ofNullable(getUnchecked(MAX_FROZEN_SUPPLY_TIME))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(
            () -> new IllegalArgumentException("not found MAX_FROZEN_SUPPLY_TIME"));
  }

  public void saveMinFrozenSupplyTime(int minFrozenSupplyTime) {
    logger.debug("MIN_FROZEN_SUPPLY_NUMBER:" + minFrozenSupplyTime);
    this.put(MIN_FROZEN_SUPPLY_TIME,
        new BytesCapsule(ByteArray.fromInt(minFrozenSupplyTime)));
  }

  public int getMinFrozenSupplyTime() {
    return Optional.ofNullable(getUnchecked(MIN_FROZEN_SUPPLY_TIME))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(
            () -> new IllegalArgumentException("not found MIN_FROZEN_SUPPLY_TIME"));
  }

  public void saveWitnessAllowanceFrozenTime(int witnessAllowanceFrozenTime) {
    logger.debug("WITNESS_ALLOWANCE_FROZEN_TIME:" + witnessAllowanceFrozenTime);
    this.put(WITNESS_ALLOWANCE_FROZEN_TIME,
        new BytesCapsule(ByteArray.fromInt(witnessAllowanceFrozenTime)));
  }

  public int getWitnessAllowanceFrozenTime() {
    return Optional.ofNullable(getUnchecked(WITNESS_ALLOWANCE_FROZEN_TIME))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(
            () -> new IllegalArgumentException("not found WITNESS_ALLOWANCE_FROZEN_TIME"));
  }

  public void saveMaintenanceTimeInterval(long timeInterval) {
    logger.debug("MAINTENANCE_TIME_INTERVAL:" + timeInterval);
    this.put(MAINTENANCE_TIME_INTERVAL,
        new BytesCapsule(ByteArray.fromLong(timeInterval)));
  }

  public long getMaintenanceTimeInterval() {
    return Optional.ofNullable(getUnchecked(MAINTENANCE_TIME_INTERVAL))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found MAINTENANCE_TIME_INTERVAL"));
  }

  public void saveAccountUpgradeCost(long accountUpgradeCost) {
    logger.debug("ACCOUNT_UPGRADE_COST:" + accountUpgradeCost);
    this.put(ACCOUNT_UPGRADE_COST,
        new BytesCapsule(ByteArray.fromLong(accountUpgradeCost)));
  }

  public long getAccountUpgradeCost() {
    return Optional.ofNullable(getUnchecked(ACCOUNT_UPGRADE_COST))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ACCOUNT_UPGRADE_COST"));
  }

  public void saveAccountUpgradeTokenCost(long accountUpgradeCost) {
    logger.debug("ACCOUNT_UPGRADE_TOKEN_COST:" + accountUpgradeCost);
    this.put(ACCOUNT_UPGRADE_TOKEN_COST,
        new BytesCapsule(ByteArray.fromLong(accountUpgradeCost)));
  }

  public long getAccountUpgradeTokenCost() {
    return Optional.ofNullable(getUnchecked(ACCOUNT_UPGRADE_TOKEN_COST))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ACCOUNT_UPGRADE_TOKEN_COST"));
  }

  public long getAccountUpgradeCost(int chargingType) {
    if(chargingType == 0) {
        return getAccountUpgradeCost();
    }
    return getAccountUpgradeTokenCost();
  }

  public void saveWitnessPayPerBlock(long pay) {
    logger.debug("WITNESS_PAY_PER_BLOCK:" + pay);
    this.put(WITNESS_PAY_PER_BLOCK,
        new BytesCapsule(ByteArray.fromLong(pay)));
  }

  public long getWitnessPayPerBlock() {
    return Optional.ofNullable(getUnchecked(WITNESS_PAY_PER_BLOCK))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found WITNESS_PAY_PER_BLOCK"));
  }

  public void saveWitnessStandbyAllowance(long allowance) {
    logger.debug("WITNESS_STANDBY_ALLOWANCE:" + allowance);
    this.put(WITNESS_STANDBY_ALLOWANCE,
        new BytesCapsule(ByteArray.fromLong(allowance)));
  }

  public long getWitnessStandbyAllowance() {
    return Optional.ofNullable(getUnchecked(WITNESS_STANDBY_ALLOWANCE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found WITNESS_STANDBY_ALLOWANCE"));
  }

  public void saveOneDayNetLimit(long oneDayNetLimit) {
    this.put(DynamicResourceProperties.ONE_DAY_NET_LIMIT,
        new BytesCapsule(ByteArray.fromLong(oneDayNetLimit)));
  }

  public long getOneDayNetLimit() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.ONE_DAY_NET_LIMIT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ONE_DAY_NET_LIMIT"));
  }

  public void savePublicNetUsage(long publicNetUsage) {
    this.put(DynamicResourceProperties.PUBLIC_NET_USAGE,
        new BytesCapsule(ByteArray.fromLong(publicNetUsage)));
  }

  public long getPublicNetUsage() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.PUBLIC_NET_USAGE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found PUBLIC_NET_USAGE"));
  }

  public void savePublicNetLimit(long publicNetLimit) {
    this.put(DynamicResourceProperties.PUBLIC_NET_LIMIT,
        new BytesCapsule(ByteArray.fromLong(publicNetLimit)));
  }

  public long getPublicNetLimit() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.PUBLIC_NET_LIMIT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found PUBLIC_NET_LIMIT"));
  }

  public void savePublicNetTime(long publicNetTime) {
    this.put(DynamicResourceProperties.PUBLIC_NET_TIME,
        new BytesCapsule(ByteArray.fromLong(publicNetTime)));
  }

  public long getPublicNetTime() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.PUBLIC_NET_TIME))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found PUBLIC_NET_TIME"));
  }

  public void saveFreeNetLimit(long freeNetLimit) {
    this.put(DynamicResourceProperties.FREE_NET_LIMIT,
        new BytesCapsule(ByteArray.fromLong(freeNetLimit)));
  }

  public long getFreeNetLimit() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.FREE_NET_LIMIT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found FREE_NET_LIMIT"));
  }

  public void saveTotalNetWeight(long totalNetWeight) {
    this.put(DynamicResourceProperties.TOTAL_NET_WEIGHT,
        new BytesCapsule(ByteArray.fromLong(totalNetWeight)));
  }

  public long getTotalNetWeight() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.TOTAL_NET_WEIGHT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_NET_WEIGHT"));
  }

  public void saveTotalEnergyWeight(long totalEnergyWeight) {
    this.put(DynamicResourceProperties.TOTAL_ENERGY_WEIGHT,
        new BytesCapsule(ByteArray.fromLong(totalEnergyWeight)));
  }

  public long getTotalEnergyWeight() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.TOTAL_ENERGY_WEIGHT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_ENERGY_WEIGHT"));
  }


  public void saveTotalNetLimit(long totalNetLimit) {
    this.put(DynamicResourceProperties.TOTAL_NET_LIMIT,
        new BytesCapsule(ByteArray.fromLong(totalNetLimit)));
  }

  public long getTotalNetLimit() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.TOTAL_NET_LIMIT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_NET_LIMIT"));
  }

  public void saveTotalEnergyLimit(long totalEnergyLimit) {
    this.put(DynamicResourceProperties.TOTAL_ENERGY_LIMIT,
        new BytesCapsule(ByteArray.fromLong(totalEnergyLimit)));

    saveTotalEnergyTargetLimit(totalEnergyLimit / 14400);
  }

  public void saveTotalEnergyLimit2(long totalEnergyLimit) {
    this.put(DynamicResourceProperties.TOTAL_ENERGY_LIMIT,
        new BytesCapsule(ByteArray.fromLong(totalEnergyLimit)));

    saveTotalEnergyTargetLimit(totalEnergyLimit / 14400);
    if (getAllowAdaptiveEnergy() == 0) {
      saveTotalEnergyCurrentLimit(totalEnergyLimit);
    }
  }

  public long getTotalEnergyLimit() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.TOTAL_ENERGY_LIMIT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_ENERGY_LIMIT"));
  }

  public void saveTotalEnergyCurrentLimit(long totalEnergyCurrentLimit) {
    this.put(DynamicResourceProperties.TOTAL_ENERGY_CURRENT_LIMIT,
        new BytesCapsule(ByteArray.fromLong(totalEnergyCurrentLimit)));
  }

  public long getTotalEnergyCurrentLimit() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.TOTAL_ENERGY_CURRENT_LIMIT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_ENERGY_CURRENT_LIMIT"));
  }

  public void saveTotalEnergyTargetLimit(long targetTotalEnergyLimit) {
    this.put(DynamicResourceProperties.TOTAL_ENERGY_TARGET_LIMIT,
        new BytesCapsule(ByteArray.fromLong(targetTotalEnergyLimit)));
  }

  public long getTotalEnergyTargetLimit() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.TOTAL_ENERGY_TARGET_LIMIT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_ENERGY_TARGET_LIMIT"));
  }

  public void saveTotalEnergyAverageUsage(long totalEnergyAverageUsage) {
    this.put(DynamicResourceProperties.TOTAL_ENERGY_AVERAGE_USAGE,
        new BytesCapsule(ByteArray.fromLong(totalEnergyAverageUsage)));
  }

  public long getTotalEnergyAverageUsage() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.TOTAL_ENERGY_AVERAGE_USAGE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_ENERGY_AVERAGE_USAGE"));
  }

  public void saveTotalEnergyAverageTime(long totalEnergyAverageTime) {
    this.put(DynamicResourceProperties.TOTAL_ENERGY_AVERAGE_TIME,
        new BytesCapsule(ByteArray.fromLong(totalEnergyAverageTime)));
  }

  public long getTotalEnergyAverageTime() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.TOTAL_ENERGY_AVERAGE_TIME))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_NET_AVERAGE_TIME"));
  }

  public void saveBlockEnergyUsage(long blockEnergyUsage) {
    this.put(DynamicResourceProperties.BLOCK_ENERGY_USAGE,
        new BytesCapsule(ByteArray.fromLong(blockEnergyUsage)));
  }

  public long getBlockEnergyUsage() {
    return Optional.ofNullable(getUnchecked(DynamicResourceProperties.BLOCK_ENERGY_USAGE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found BLOCK_ENERGY_USAGE"));
  }

  public void saveEnergyFee(long totalEnergyFee) {
    this.put(ENERGY_FEE,
        new BytesCapsule(ByteArray.fromLong(totalEnergyFee)));
  }

  public long getEnergyFee() {
    return Optional.ofNullable(getUnchecked(ENERGY_FEE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ENERGY_FEE"));
  }

  public void saveEnergyTokenFee(long totalEnergyFee) {
    this.put(ENERGY_TOKEN_FEE,
        new BytesCapsule(ByteArray.fromLong(totalEnergyFee)));
  }

  public long getEnergyTokenFee() {
    return Optional.ofNullable(getUnchecked(ENERGY_TOKEN_FEE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ENERGY_TOKEN_FEE"));
  }

  public long getEnergyFee(int chargingType) {
    if(chargingType == 0) {
        return getEnergyFee();
    }

    return getEnergyTokenFee();
  }

  public void saveMaxCpuTimeOfOneTx(long time) {
    this.put(MAX_CPU_TIME_OF_ONE_TX,
        new BytesCapsule(ByteArray.fromLong(time)));
  }

  public long getMaxCpuTimeOfOneTx() {
    return Optional.ofNullable(getUnchecked(MAX_CPU_TIME_OF_ONE_TX))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found MAX_CPU_TIME_OF_ONE_TX"));
  }

  public void saveCreateAccountFee(long fee) {
    this.put(CREATE_ACCOUNT_FEE,
        new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public long getCreateAccountFee() {
    return Optional.ofNullable(getUnchecked(CREATE_ACCOUNT_FEE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found CREATE_ACCOUNT_FEE"));
  }

  public void saveCreateAccountSunTokenFee(long fee) {
    this.put(CREATE_ACCOUNT_SUNTOKEN_FEE,
            new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public long getCreateAccountSunTokenFee() {
    return Optional.ofNullable(getUnchecked(CREATE_ACCOUNT_SUNTOKEN_FEE))
            .map(BytesCapsule::getData)
            .map(ByteArray::toLong)
            .orElseThrow(
                    () -> new IllegalArgumentException("not found CREATE_ACCOUNT_SUNTOKEN_FEE"));
  }

  public long getCreateAccountFee(int chargingType) {
    if(chargingType == 0) {
        return getCreateAccountFee();
    }

    return getCreateAccountSunTokenFee();
  }

  public void saveCreateNewAccountFeeInSystemContract(long fee) {
    this.put(CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT,
        new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public long getCreateNewAccountFeeInSystemContract() {
    return Optional.ofNullable(getUnchecked(CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException(
                "not found CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT"));
  }

  public void saveCreateNewAccountTokenFeeInSystemContract(long fee) {
    this.put(CREATE_NEW_ACCOUNT_TOKEN_FEE_IN_SYSTEM_CONTRACT,
        new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public long getCreateNewAccountTokenFeeInSystemContract() {
    return Optional.ofNullable(getUnchecked(CREATE_NEW_ACCOUNT_TOKEN_FEE_IN_SYSTEM_CONTRACT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException(
                "not found CREATE_NEW_ACCOUNT_TOKEN_FEE_IN_SYSTEM_CONTRACT"));
  }

  public void saveCreateNewAccountBandwidthRate(long rate) {
    this.put(CREATE_NEW_ACCOUNT_BANDWIDTH_RATE,
        new BytesCapsule(ByteArray.fromLong(rate)));
  }

  public long getCreateNewAccountBandwidthRate() {
    return Optional.ofNullable(getUnchecked(CREATE_NEW_ACCOUNT_BANDWIDTH_RATE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found CREATE_NEW_ACCOUNT_BANDWIDTH_RATE2"));
  }

  public void saveCreateNewAccountEnergyRate(long rate) {
    this.put(CREATE_NEW_ACCOUNT_ENERGY_BYTE_RATE,
        new BytesCapsule(ByteArray.fromLong(rate)));
  }

  public long getCreateNewAccountEnergyRate() {
    return Optional.ofNullable(getUnchecked(CREATE_NEW_ACCOUNT_ENERGY_BYTE_RATE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found CREATE_NEW_ACCOUNT_ENERGY_RATE"));
  }

  public void saveCreateNewAccountSunTokenEnergyRate(long rate) {
    this.put(CREATE_NEW_ACCOUNT_SUNTOKEN_ENERGY_BYTE_RATE,
            new BytesCapsule(ByteArray.fromLong(rate)));
  }

  public long getCreateNewAccountSunTokenEnergyRate() {
    return Optional.ofNullable(getUnchecked(CREATE_NEW_ACCOUNT_SUNTOKEN_ENERGY_BYTE_RATE))
            .map(BytesCapsule::getData)
            .map(ByteArray::toLong)
            .orElseThrow(
                    () -> new IllegalArgumentException("not found CREATE_NEW_ACCOUNT_SUNTOKEN_ENERGY_BYTE_RATE"));
  }

  public long getCreateNewAccountEnergyRate(int chargingType) {
    if(chargingType == 0) {
      return getCreateNewAccountEnergyRate();
    }

    return getCreateNewAccountSunTokenEnergyRate();
  }

  public void saveTransactionFee(long fee) {
    this.put(TRANSACTION_FEE,
        new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public long getTransactionFee() {
    return Optional.ofNullable(getUnchecked(TRANSACTION_FEE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TRANSACTION_FEE"));
  }

  public void saveTransactionSunTokenFee(long fee) {
    this.put(TRANSACTION_SUNTOKEN_FEE,
            new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public long getTransactionSunTokenFee() {
    return Optional.ofNullable(getUnchecked(TRANSACTION_SUNTOKEN_FEE))
            .map(BytesCapsule::getData)
            .map(ByteArray::toLong)
            .orElseThrow(
                    () -> new IllegalArgumentException("not found TRANSACTION_SUNTOKEN_FEE"));
  }

  public long getTransactionFee(int chargingType) {
      if(chargingType == 0) {
          return getTransactionFee();
      }
      return getTransactionSunTokenFee();
  }

  public void saveAssetIssueFee(long fee) {
    this.put(ASSET_ISSUE_FEE,
        new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public void saveUpdateAccountPermissionFee(long fee) {
    this.put(UPDATE_ACCOUNT_PERMISSION_FEE,
        new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public void saveUpdateAccountPermissionTokenFee(long fee) {
    this.put(UPDATE_ACCOUNT_PERMISSION_TOKEN_FEE,
        new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public void saveMultiSignFee(long fee) {
    this.put(MULTI_SIGN_FEE,
        new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public void saveMultiSignTokenFee(long fee) {
    this.put(MULTI_SIGN_TOKEN_FEE,
        new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public long getAssetIssueFee() {
    return Optional.ofNullable(getUnchecked(ASSET_ISSUE_FEE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ASSET_ISSUE_FEE"));
  }

  public long getUpdateAccountPermissionFee() {
    return Optional.ofNullable(getUnchecked(UPDATE_ACCOUNT_PERMISSION_FEE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found UPDATE_ACCOUNT_PERMISSION_FEE"));
  }

  public long getUpdateAccountPermissionTokenFee() {
    return Optional.ofNullable(getUnchecked(UPDATE_ACCOUNT_PERMISSION_TOKEN_FEE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found UPDATE_ACCOUNT_PERMISSION_TOKEN_FEE"));
  }

  public long getMultiSignFee() {
    return Optional.ofNullable(getUnchecked(MULTI_SIGN_FEE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found MULTI_SIGN_FEE"));
  }

  public long getMultiSignTokenFee() {
    return Optional.ofNullable(getUnchecked(MULTI_SIGN_TOKEN_FEE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found MULTI_SIGN_TOKEN_FEE"));
  }

  public long getMultiSignFee(int chargingType) {
    if(chargingType == 0) {
      return getMultiSignFee();
    }

    return getMultiSignTokenFee();
  }

  public void saveExchangeCreateFee(long fee) {
    this.put(EXCHANGE_CREATE_FEE,
        new BytesCapsule(ByteArray.fromLong(fee)));
  }

  public long getExchangeCreateFee() {
    return Optional.ofNullable(getUnchecked(EXCHANGE_CREATE_FEE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found EXCHANGE_CREATE_FEE"));
  }

  public void saveExchangeBalanceLimit(long limit) {
    this.put(EXCHANGE_BALANCE_LIMIT,
        new BytesCapsule(ByteArray.fromLong(limit)));
  }

  public long getExchangeBalanceLimit() {
    return Optional.ofNullable(getUnchecked(EXCHANGE_BALANCE_LIMIT))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found EXCHANGE_BALANCE_LIMIT"));
  }

  public void saveTotalTransactionCost(long value) {
    this.put(TOTAL_TRANSACTION_COST,
        new BytesCapsule(ByteArray.fromLong(value)));
  }

  public long getTotalTransactionCost() {
    return Optional.ofNullable(getUnchecked(TOTAL_TRANSACTION_COST))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_TRANSACTION_COST"));
  }

  public void saveTotalCreateAccountFee(long value) {
    this.put(TOTAL_CREATE_ACCOUNT_COST,
        new BytesCapsule(ByteArray.fromLong(value)));
  }

  public long getTotalCreateAccountCost() {
    return Optional.ofNullable(getUnchecked(TOTAL_CREATE_ACCOUNT_COST))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_CREATE_ACCOUNT_COST"));
  }

  public void saveTotalCreateWitnessFee(long value) {
    this.put(TOTAL_CREATE_WITNESS_COST,
        new BytesCapsule(ByteArray.fromLong(value)));
  }

  public long getTotalCreateWitnessCost() {
    return Optional.ofNullable(getUnchecked(TOTAL_CREATE_WITNESS_COST))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_CREATE_WITNESS_COST"));
  }

  public void saveTotalStoragePool(long trx) {
    this.put(TOTAL_STORAGE_POOL,
        new BytesCapsule(ByteArray.fromLong(trx)));
  }

  public long getTotalStoragePool() {
    return Optional.ofNullable(getUnchecked(TOTAL_STORAGE_POOL))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_STORAGE_POOL"));
  }

  public void saveTotalStorageTax(long trx) {
    this.put(TOTAL_STORAGE_TAX,
        new BytesCapsule(ByteArray.fromLong(trx)));
  }

  public long getTotalStorageTax() {
    return Optional.ofNullable(getUnchecked(TOTAL_STORAGE_TAX))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_STORAGE_TAX"));
  }

  public void saveTotalStorageReserved(long bytes) {
    this.put(TOTAL_STORAGE_RESERVED,
        new BytesCapsule(ByteArray.fromLong(bytes)));
  }

  public long getTotalStorageReserved() {
    return Optional.ofNullable(getUnchecked(TOTAL_STORAGE_RESERVED))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_STORAGE_RESERVED"));
  }

  public void saveStorageExchangeTaxRate(long rate) {
    this.put(STORAGE_EXCHANGE_TAX_RATE,
        new BytesCapsule(ByteArray.fromLong(rate)));
  }

  public long getStorageExchangeTaxRate() {
    return Optional.ofNullable(getUnchecked(STORAGE_EXCHANGE_TAX_RATE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found STORAGE_EXCHANGE_TAX_RATE"));
  }

  public void saveRemoveThePowerOfTheGr(long rate) {
    this.put(REMOVE_THE_POWER_OF_THE_GR,
        new BytesCapsule(ByteArray.fromLong(rate)));
  }

  public long getRemoveThePowerOfTheGr() {
    return Optional.ofNullable(getUnchecked(REMOVE_THE_POWER_OF_THE_GR))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found REMOVE_THE_POWER_OF_THE_GR"));
  }

  public void saveAllowDelegateResource(long value) {
    this.put(ALLOW_DELEGATE_RESOURCE,
        new BytesCapsule(ByteArray.fromLong(value)));
  }

  public long getAllowDelegateResource() {
    return Optional.ofNullable(getUnchecked(ALLOW_DELEGATE_RESOURCE))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ALLOW_DELEGATE_RESOURCE"));
  }

  public void saveAllowAdaptiveEnergy(long value) {
    this.put(ALLOW_ADAPTIVE_ENERGY,
        new BytesCapsule(ByteArray.fromLong(value)));
  }

  public long getAllowAdaptiveEnergy() {
    return Optional.ofNullable(getUnchecked(ALLOW_ADAPTIVE_ENERGY))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ALLOW_ADAPTIVE_ENERGY"));
  }

  public void saveAllowTvmTransferTrc10(long value) {
    this.put(ALLOW_TVM_TRANSFER_TRC10,
        new BytesCapsule(ByteArray.fromLong(value)));
  }

  public long getAllowTvmTransferTrc10() {
    return Optional.ofNullable(getUnchecked(ALLOW_TVM_TRANSFER_TRC10))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ALLOW_TVM_TRANSFER_TRC10"));
  }

  public void saveAvailableContractType(byte[] value) {
    this.put(AVAILABLE_CONTRACT_TYPE,
        new BytesCapsule(value));
  }

  public byte[] getAvailableContractType() {
    return Optional.ofNullable(getUnchecked(AVAILABLE_CONTRACT_TYPE))
        .map(BytesCapsule::getData)
        .orElseThrow(
            () -> new IllegalArgumentException("not found AVAILABLE_CONTRACT_TYPE"));
  }


  public void saveActiveDefaultOperations(byte[] value) {
    this.put(ACTIVE_DEFAULT_OPERATIONS,
        new BytesCapsule(value));
  }

  public byte[] getActiveDefaultOperations() {
    return Optional.ofNullable(getUnchecked(ACTIVE_DEFAULT_OPERATIONS))
        .map(BytesCapsule::getData)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ACTIVE_DEFAULT_OPERATIONS"));
  }


  public boolean supportDR() {
    return getAllowDelegateResource() == 1L;
  }

  public void saveTotalSignNum(int num) {
    this.put(DynamicPropertiesStore.TOTAL_SIGN_NUM,
        new BytesCapsule(ByteArray.fromInt(num)));
  }

  public int getTotalSignNum() {
    return Optional.ofNullable(getUnchecked(TOTAL_SIGN_NUM))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(
            () -> new IllegalArgumentException("not found TOTAL_SIGN_NUM"));
  }

  public void saveAllowMultiSign(long allowMultiSing) {
    this.put(ALLOW_MULTI_SIGN,
        new BytesCapsule(ByteArray.fromLong(allowMultiSing)));
  }

  public long getAllowMultiSign() {
    return Optional.ofNullable(getUnchecked(ALLOW_MULTI_SIGN))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found ALLOW_MULTI_SIGN"));
  }

  public void saveBlockFilledSlots(int[] blockFilledSlots) {
    logger.debug("blockFilledSlots:" + intArrayToString(blockFilledSlots));
    this.put(BLOCK_FILLED_SLOTS,
        new BytesCapsule(ByteArray.fromString(intArrayToString(blockFilledSlots))));
  }

  public int[] getBlockFilledSlots() {
    return Optional.ofNullable(getUnchecked(BLOCK_FILLED_SLOTS))
        .map(BytesCapsule::getData)
        .map(ByteArray::toStr)
        .map(this::stringToIntArray)
        .orElseThrow(
            () -> new IllegalArgumentException("not found latest SOLIDIFIED_BLOCK_NUM timestamp"));
  }

  public int getBlockFilledSlotsNumber() {
    return ChainConstant.BLOCK_FILLED_SLOTS_NUMBER;
  }

  public void applyBlock(boolean fillBlock) {
    int[] blockFilledSlots = getBlockFilledSlots();
    int blockFilledSlotsIndex = getBlockFilledSlotsIndex();
    blockFilledSlots[blockFilledSlotsIndex] = fillBlock ? 1 : 0;
    saveBlockFilledSlotsIndex((blockFilledSlotsIndex + 1) % getBlockFilledSlotsNumber());
    saveBlockFilledSlots(blockFilledSlots);
  }

  public int calculateFilledSlotsCount() {
    int[] blockFilledSlots = getBlockFilledSlots();
    return 100 * IntStream.of(blockFilledSlots).sum() / getBlockFilledSlotsNumber();
  }

  public void saveLatestSolidifiedBlockNum(long number) {
    this.put(LATEST_SOLIDIFIED_BLOCK_NUM, new BytesCapsule(ByteArray.fromLong(number)));
  }


  public long getLatestSolidifiedBlockNum() {
    return Optional.ofNullable(getUnchecked(LATEST_SOLIDIFIED_BLOCK_NUM))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found latest SOLIDIFIED_BLOCK_NUM"));
  }

  public void saveLatestProposalNum(long number) {
    this.put(LATEST_PROPOSAL_NUM, new BytesCapsule(ByteArray.fromLong(number)));
  }

  public long getLatestProposalNum() {
    return Optional.ofNullable(getUnchecked(LATEST_PROPOSAL_NUM))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found latest PROPOSAL_NUM"));
  }

  public void saveLatestExchangeNum(long number) {
    this.put(LATEST_EXCHANGE_NUM, new BytesCapsule(ByteArray.fromLong(number)));
  }

  public long getLatestExchangeNum() {
    return Optional.ofNullable(getUnchecked(LATEST_EXCHANGE_NUM))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found latest EXCHANGE_NUM"));
  }

  /**
   * get timestamp of creating global latest block.
   */
  public long getLatestBlockHeaderTimestamp() {
    return Optional.ofNullable(getUnchecked(LATEST_BLOCK_HEADER_TIMESTAMP))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(() -> new IllegalArgumentException("not found latest block header timestamp"));
  }

  /**
   * get number of global latest block.
   */
  public long getLatestBlockHeaderNumber() {
    return Optional.ofNullable(getUnchecked(LATEST_BLOCK_HEADER_NUMBER))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(() -> new IllegalArgumentException("not found latest block header number"));
  }

  public int getStateFlag() {
    return Optional.ofNullable(getUnchecked(STATE_FLAG))
        .map(BytesCapsule::getData)
        .map(ByteArray::toInt)
        .orElseThrow(() -> new IllegalArgumentException("not found maintenance flag"));
  }

  /**
   * get id of global latest block.
   */

  public Sha256Hash getLatestBlockHeaderHash() {
    byte[] blockHash = Optional.ofNullable(getUnchecked(LATEST_BLOCK_HEADER_HASH))
        .map(BytesCapsule::getData)
        .orElseThrow(() -> new IllegalArgumentException("not found block hash"));
    return Sha256Hash.wrap(blockHash);
  }

  /**
   * save timestamp of creating global latest block.
   */
  public void saveLatestBlockHeaderTimestamp(long t) {
    logger.info("update latest block header timestamp = {}", t);
    this.put(LATEST_BLOCK_HEADER_TIMESTAMP, new BytesCapsule(ByteArray.fromLong(t)));
  }

  /**
   * save number of global latest block.
   */
  public void saveLatestBlockHeaderNumber(long n) {
    logger.info("update latest block header number = {}", n);
    this.put(LATEST_BLOCK_HEADER_NUMBER, new BytesCapsule(ByteArray.fromLong(n)));
  }

  /**
   * save id of global latest block.
   */
  public void saveLatestBlockHeaderHash(ByteString h) {
    logger.info("update latest block header id = {}", ByteArray.toHexString(h.toByteArray()));
    this.put(LATEST_BLOCK_HEADER_HASH, new BytesCapsule(h.toByteArray()));
    if (revokingDB.getUnchecked(LATEST_BLOCK_HEADER_HASH).length == 32) {
    }
  }

  public void saveStateFlag(int n) {
    logger.info("update state flag = {}", n);
    this.put(STATE_FLAG, new BytesCapsule(ByteArray.fromInt(n)));
  }


  public long getNextMaintenanceTime() {
    return Optional.ofNullable(getUnchecked(NEXT_MAINTENANCE_TIME))
        .map(BytesCapsule::getData)
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found NEXT_MAINTENANCE_TIME"));
  }

  public long getMaintenanceSkipSlots() {
    return Parameter.ChainConstant.MAINTENANCE_SKIP_SLOTS;
  }

  public void saveNextMaintenanceTime(long nextMaintenanceTime) {
    this.put(NEXT_MAINTENANCE_TIME,
        new BytesCapsule(ByteArray.fromLong(nextMaintenanceTime)));
  }


  public void updateNextMaintenanceTime(long blockTime) {
    long maintenanceTimeInterval = getMaintenanceTimeInterval();

    long currentMaintenanceTime = getNextMaintenanceTime();
    long round = (blockTime - currentMaintenanceTime) / maintenanceTimeInterval;
    long nextMaintenanceTime = currentMaintenanceTime + (round + 1) * maintenanceTimeInterval;
    saveNextMaintenanceTime(nextMaintenanceTime);

    logger.info(
        "do update nextMaintenanceTime,currentMaintenanceTime:{}, blockTime:{},nextMaintenanceTime:{}",
        new DateTime(currentMaintenanceTime), new DateTime(blockTime),
        new DateTime(nextMaintenanceTime)
    );
  }

  //The unit is trx
  public void addTotalNetWeight(long amount) {
    long totalNetWeight = getTotalNetWeight();
    totalNetWeight += amount;
    saveTotalNetWeight(totalNetWeight);
  }

  //The unit is trx
  public void addTotalEnergyWeight(long amount) {
    long totalEnergyWeight = getTotalEnergyWeight();
    totalEnergyWeight += amount;
    saveTotalEnergyWeight(totalEnergyWeight);
  }

  public void addTotalCreateAccountCost(long fee) {
    long newValue = getTotalCreateAccountCost() + fee;
    saveTotalCreateAccountFee(newValue);
  }

  public void addTotalCreateWitnessCost(long fee) {
    long newValue = getTotalCreateWitnessCost() + fee;
    saveTotalCreateWitnessFee(newValue);
  }

  public void addTotalTransactionCost(long fee) {
    long newValue = getTotalTransactionCost() + fee;
    saveTotalTransactionCost(newValue);
  }

  public void forked() {
    put(FORK_CONTROLLER, new BytesCapsule(Boolean.toString(true).getBytes()));
  }

  public void statsByVersion(int version, byte[] stats) {
    String statsKey = FORK_PREFIX + version;
    put(statsKey.getBytes(), new BytesCapsule(stats));
  }

  public byte[] statsByVersion(int version) {
    String statsKey = FORK_PREFIX + version;
    return revokingDB.getUnchecked(statsKey.getBytes());
  }

  public boolean getForked() {
    byte[] value = revokingDB.getUnchecked(FORK_CONTROLLER);
    return value == null ? Boolean.FALSE : Boolean.valueOf(new String(value));
  }
}
