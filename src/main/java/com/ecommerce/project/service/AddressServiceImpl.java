package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl  implements AddressService{

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AddressRepository addressRepository;
    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address=modelMapper.map(addressDTO,Address.class);

        //update address for user
        List<Address> addressList=user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);


        address.setUser(user);
        Address savedAddress=addressRepository.save(address);

        return modelMapper.map(savedAddress,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {
        List<Address>addresses=addressRepository.findAll();

        return addresses.stream()
                .map(address -> modelMapper.map(address,AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddressesById(Long addressId) {
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        return modelMapper.map(address,AddressDTO.class);

    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address>addresses=user.getAddresses();

        return addresses.stream()
                .map(address -> modelMapper.map(address,AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address addressFromDatabase=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        addressFromDatabase.setCity(addressDTO.getCity());
        addressFromDatabase.setPincode(addressDTO.getPincode());
        addressFromDatabase.setPincode(addressDTO.getPincode());
        addressFromDatabase.setCountry(addressDTO.getCountry());
        addressFromDatabase.setState(addressDTO.getState());
        addressFromDatabase.setStreet(addressDTO.getStreet());

        //save address to db
        Address updatedAddress=addressRepository.save(addressFromDatabase);

        //once it saved it udpated against the user
        User user=addressFromDatabase.getUser();
        //first we remvoe the address in user if id match then update the address to the user object
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);

        userRepository.save(user);

        return modelMapper.map(updatedAddress,AddressDTO.class);

    }

    @Override
    public String deleteAddress(Long addressId) {
        Address addressFromDatabase=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        User user=addressFromDatabase.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        userRepository.save(user);

        addressRepository.delete(addressFromDatabase);

        return "Address deleted successfully with addressId: "+addressId;
    }
}
