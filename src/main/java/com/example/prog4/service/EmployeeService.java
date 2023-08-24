package com.example.prog4.service;

import com.example.prog4.controller.mapper.EmployeeMapper;
import com.example.prog4.model.EmployeeFilter;
import com.example.prog4.model.exception.NotFoundException;
import com.example.prog4.repository.postgres1.EmployeeRepository;
import com.example.prog4.repository.postgres1.dao.EmployeeManagerDao;
import com.example.prog4.repository.postgres1.entity.Employee;
import com.example.prog4.repository.postgres2.CNAPSRepository;
import com.example.prog4.repository.postgres2.entity.CNAPSEmployee;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CNAPSRepository cnapsRepository;

    private EmployeeMapper mapper;
    private EmployeeManagerDao employeeManagerDao;


    @Transactional
    public Employee getOne(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found id=" + id));
    }

    @Transactional
    public List<Employee> getAll(EmployeeFilter filter) {
        Sort sort = Sort.by(filter.getOrderDirection(), filter.getOrderBy().toString());
        Pageable pageable = PageRequest.of(filter.getIntPage() - 1, filter.getIntPerPage(), sort);
        return employeeManagerDao.findByCriteria(
                filter.getLastName(),
                filter.getFirstName(),
                filter.getCountryCode(),
                filter.getSex(),
                filter.getPosition(),
                filter.getEntrance(),
                filter.getDeparture(),
                pageable
        );
    }

    public void saveOne(com.example.prog4.model.Employee employee) {
        Employee saved = employeeRepository.save(mapper.toDomain(employee));
        cnapsRepository.save(CNAPSEmployee.builder()
                .endToEndId(saved.getId())
                .address(employee.getAddress())
                .cin(employee.getCin())
                .cnaps(employee.getCnaps())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .birthDate(employee.getBirthDate())
                .childrenNumber(employee.getChildrenNumber())
                .personalEmail(employee.getPersonalEmail())
                .professionalEmail(employee.getProfessionalEmail())
                .entranceDate(employee.getEntranceDate())
                .departureDate(employee.getDepartureDate())
                .build());
    }

    public String getEmployeeCnaps(String idEmployee) {
        CNAPSEmployee cnapsEmployee = cnapsRepository.findById(idEmployee)
                .orElseThrow(() -> new NotFoundException("Not found id=" + idEmployee));
        return cnapsEmployee.getCnaps();
    }
}
