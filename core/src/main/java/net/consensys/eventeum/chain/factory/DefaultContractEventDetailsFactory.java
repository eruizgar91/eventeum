package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.converter.EventParameterConverter;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.parameter.EventParameter;
import net.consensys.eventeum.dto.event.parameter.NumberParameter;
import net.consensys.eventeum.dto.event.parameter.StringParameter;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultContractEventDetailsFactory implements ContractEventDetailsFactory {

    private EventParameterConverter<Type> parameterConverter;
    private EventConfirmationConfig eventConfirmationConfig;
    private String networkName;

    public DefaultContractEventDetailsFactory(EventParameterConverter<Type> parameterConverter,
                                              EventConfirmationConfig eventConfirmationConfig,
                                              String networkName) {
        this.parameterConverter = parameterConverter;
        this.eventConfirmationConfig = eventConfirmationConfig;
        this.networkName = networkName;
    }

    @Override
    public ContractEventDetails createEventDetails(ContractEventFilter eventFilter, Log log) {
        final ContractEventSpecification eventSpec = eventFilter.getEventSpecification();

        final List<EventParameter> nonIndexed = typeListToParameterList(getNonIndexedParametersFromLog(eventSpec, log));
        final List<EventParameter> indexed = typeListToParameterList(getIndexedParametersFromLog(eventSpec, log));

        addNameToParameter(eventSpec.getIndexedParameterDefinitions(), indexed);
        addNameToParameter(eventSpec.getNonIndexedParameterDefinitions(), nonIndexed);
        final ContractEventDetails eventDetails = new ContractEventDetails();
        eventDetails.setName(eventSpec.getEventName());
        eventDetails.setFilterId(eventFilter.getId());
        eventDetails.setNonIndexedParameters(nonIndexed);
        eventDetails.setIndexedParameters(indexed);
        eventDetails.setAddress(Keys.toChecksumAddress(log.getAddress()));
        eventDetails.setLogIndex(log.getLogIndex());
        eventDetails.setTransactionHash(log.getTransactionHash());
        eventDetails.setBlockNumber(log.getBlockNumber());
        eventDetails.setBlockHash(log.getBlockHash());
        eventDetails.setEventSpecificationSignature(Web3jUtil.getSignature(eventSpec));
        eventDetails.setNetworkName(this.networkName);
        eventDetails.setNodeName(eventFilter.getNode());

        if (log.isRemoved()) {
            eventDetails.setStatus(ContractEventStatus.INVALIDATED);
        } else if (eventConfirmationConfig.getBlocksToWaitForConfirmation().equals(BigInteger.ZERO)) {
            //Set to confirmed straight away if set to zero confirmations
            eventDetails.setStatus(ContractEventStatus.CONFIRMED);
        } else {
            eventDetails.setStatus(ContractEventStatus.UNCONFIRMED);
        }

        return eventDetails;
    }

    private void addNameToParameter(List<ParameterDefinition> a, List<EventParameter> l) {
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).getClass() == NumberParameter.class) {
                l.set(i, new NumberParameter(l.get(i).getType(), (BigInteger) l.get(i).getValue(), a.get(i).getName()));
            } else {
                l.set(i, new StringParameter(l.get(i).getType(), l.get(i).getValueString(), a.get(i).getName()));

            }
        }
    }

    private List<EventParameter> typeListToParameterList(List<Type> typeList) {
        if (isNullOrEmpty(typeList)) {
            return Collections.EMPTY_LIST;
        }

        return typeList
                .stream()
                .map(type -> parameterConverter.convert(type))
                .collect(Collectors.toList());
    }

    private List<Type> getNonIndexedParametersFromLog(ContractEventSpecification eventSpec, Log log) {
        if (isNullOrEmpty(eventSpec.getNonIndexedParameterDefinitions())) {
            return Collections.EMPTY_LIST;
        }

        return FunctionReturnDecoder.decode(
                log.getData(),
                Utils.convert(Web3jUtil.getTypeReferencesFromParameterDefinitions(
                        eventSpec.getNonIndexedParameterDefinitions())));
    }

    private List<Type> getIndexedParametersFromLog(ContractEventSpecification eventSpec, Log log) {
        if (isNullOrEmpty(eventSpec.getIndexedParameterDefinitions())) {
            return Collections.EMPTY_LIST;
        }

        final List<String> encodedParameters = log.getTopics().subList(1, log.getTopics().size());
        final List<ParameterDefinition> definitions = eventSpec.getIndexedParameterDefinitions();

        return IntStream.range(0, encodedParameters.size())
                .mapToObj(i -> FunctionReturnDecoder.decodeIndexedValue(encodedParameters.get(i),
                        Web3jUtil.getTypeReferenceFromParameterType(definitions.get(i).getType())))
                .collect(Collectors.toList());
    }

    private boolean isNullOrEmpty(List<?> toCheck) {
        return toCheck == null || toCheck.isEmpty();
    }
}