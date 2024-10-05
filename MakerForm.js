import { useEffect, useState } from 'react';
import '../css/makerFormData.css';

const Modal = ({ mode, user, onClose, onSave, accounts, approvers, handleAccountChange, handleAdd }) => {
    const [formData, setFormData] = useState(user || {});
    const [approverDetails, setApproverDetails] = useState([]);
    const [selectedGroups, setSelectedGroups] = useState([]);

    useEffect(() => {
        setFormData(user || {});
        setApproverDetails([]); // Clear approvers when user changes
        setSelectedGroups([]); // Clear selected groups
    }, [user]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));

        // Fetch approvers when account is selected
        if (name === 'serviceCdId' && value) {
            handleAdd(value);
            handleAccountChange(value).then((approvers) => {
                setApproverDetails(approvers);
            });
        }
        if (name === 'accountNo' && value) {
            handleAccountChange(value);
        }
    };

    const handleApproverSelection = (approverId, index) => {
        setApproverDetails(prev => {
            const updatedDetails = [...prev];
            updatedDetails[index].selected = !updatedDetails[index].selected; // Toggle selection
            return updatedDetails;
        });
    };

    const handleGroupSelection = (e, groupName) => {
        const { checked } = e.target;
        setSelectedGroups((prev) => {
            if (checked) {
                return [...prev, { name: groupName, dailyLimit: '' }];
            } else {
                return prev.filter(group => group.name !== groupName);
            }
        });
    };

    const handleDailyLimitChange = (e, groupName) => {
        const value = e.target.value;
        setSelectedGroups((prev) => 
            prev.map(group => 
                group.name === groupName ? { ...group, dailyLimit: value } : group
            )
        );
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        const userData = {
            ...formData,
            approverList: approverDetails.filter(a => a.selected).map(a => a.approverId).join(","),
            selectedGroups: selectedGroups.map(group => ({
                name: group.name,
                dailyLimit: group.dailyLimit
            })),
        };
        onSave(userData);
        onClose();
    };

    return (
        <div className="modal">
            <div className="modal-content">
                <span className="close" onClick={onClose}>&times;</span>
                <h2>{mode === 'view' ? 'User Details' : mode === 'update' ? 'Update User' : 'Add New User'}</h2>
                <form className='modal-form' onSubmit={handleSubmit}>
                    <div className='company-details'>
                        <div className="form-group">
                            <label>Company ID</label>
                            <input name="serviceCdId" type="text" value={formData.serviceCdId || ''} onChange={handleChange} readOnly={mode === 'view'} required />
                        </div>
                        <div className="form-group">
                            <label>Active Flag</label>
                            <input name="activeFlag" type="text" value={formData.activeFlag || 'Y'} onChange={handleChange} readOnly={mode === 'view' || mode === 'add'} />
                        </div>
                    </div>
                    <div className='accounts-details'>
                        <div className="form-group">
                            <label>Account Number</label>
                            <select name="accountNo" value={formData.accountNo || ''} onChange={handleChange} required>
                                <option value="">Select Account</option>
                                {accounts.map((account, index) => (
                                    <option key={`${account.trim()}-${index}`} value={account.trim()}>
                                        {account.trim()}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                    <div className='limit-setup-details'>
                        <div className="form-group">
                            <table className='left'>
                                <thead>
                                    <tr>
                                        <th className='modal-checkbox'></th>
                                        <th>Group Service Name</th>
                                        <th>Daily Limit (In Rs.)</th>
                                        <th>Amount (In Words)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {['Financial Transactions - Bulk Transaction and Group', 'Financial Transactions - Onscreen', 'Salary Management', 'Financial Transactions - Tax'].map((group, index) => (
                                        <tr key={index}>
                                            <td><input type='checkbox' onChange={(e) => handleGroupSelection(e, group)} /></td>
                                            <td><b>{group}</b></td>
                                            <td><input type='text' onChange={(e) => handleDailyLimitChange(e, group)} placeholder='Daily Limit' /></td>
                                            <td><input type='text' /></td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            <table className='right-table'>
                                <thead>
                                    <tr>
                                        <th></th>
                                        <th>Approver ID</th>
                                        <th>Approver Name</th>
                                        <th>Approver Role</th>
                                        <th>Per Transaction Limit (In Rs.)</th>
                                        <th>Amount (In Words)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {Array.isArray(approverDetails) && approverDetails.map((approver, index) => (
                                        <tr key={approver.approverId}>
                                            <td><input type='checkbox' onChange={() => handleApproverSelection(approver.approverId, index)} /></td>
                                            <td>{approver.approverId}</td>
                                            <td>{approver.approverName}</td>
                                            <td>{approver.approverRole || ''}</td>
                                            <td><input type='text' onChange={(e) => handleDailyLimitChange(e, index)} placeholder='Daily Limit' /></td>
                                            <td><input type='text' /></td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div className='maker-author-details' style={{ display: mode === 'add' ? 'none' : 'block' }}>
                        <div className="form-group">
                            <label>Maker Code:</label>
                            [{formData.makerCd}]
                        </div>
                        <div className="form-group">
                            <label>Maker Date</label>
                            [{formData.makerDt}]
                        </div>
                    </div>
                    <div className="button-group">
                        {mode === 'add' ? (
                            <button type="submit">Add User</button>
                        ) : (
                            <button type="submit" style={{ display: mode === 'view' ? 'none' : 'inline-block' }}>Save Changes</button>
                        )}
                        <button type="button" onClick={onClose}>Cancel</button>
                    </div>
                </form>
            </div>
        </div>
    );
};




const Form = () => {
    const [data, setData] = useState([]);
    const [accounts, setAccounts] = useState([]);
    const [approvers, setApprovers] = useState([]);
    const [searchInput, setSearchInput] = useState({
        companyID: '',
        accountNo: '',
        groupName: '',
        status: 'all',
    });
    const [filteredData, setFilteredData] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [selectedUsers, setSelectedUsers] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('view');
    const [formData, setFormData] = useState({});
    const recordsPerPage = 10;

    useEffect(() => {
        const fetchData = async () => {
            try {
                const resp = await fetch("http://192.168.0.108:8081/users/all");
                if (!resp.ok) throw new Error(`HTTP error! status: ${resp.status}`);
                const result = await resp.json();
                setData(result);
                setFilteredData(result);
            } catch (err) {
                console.log("Error fetching data", err);
            }
        };
        fetchData();
    }, []);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setSearchInput((prev) => ({ ...prev, [name]: value }));
    };

    const handleSearch = () => {
        const results = data.filter(item => {
            const matchesCompanyID = item.serviceCdId.toString().includes(searchInput.companyID);
            const matchesAccountNo = item.accountNo.toString().includes(searchInput.accountNo);
            const matchesGroupName = item.groupName.toLowerCase().includes(searchInput.groupName.toLowerCase());
            const matchesStatus = searchInput.status === 'all' || item.authStatus === searchInput.status;

            return matchesCompanyID && matchesAccountNo && matchesGroupName && matchesStatus;
        });
        setFilteredData(results);
        setCurrentPage(1);
    };

    const handleClear = () => {
        setSearchInput({ companyID: '', accountNo: '', groupName: '', status: 'all' });
        setFilteredData(data);
        setCurrentPage(1);
        setSelectedUsers([]);
    };

    const indexOfLastRecord = currentPage * recordsPerPage;
    const indexOfFirstRecord = indexOfLastRecord - recordsPerPage;
    const currentRecords = filteredData.slice(indexOfFirstRecord, indexOfLastRecord);
    const totalPages = Math.ceil(filteredData.length / recordsPerPage);

    const handleCheckboxChange = (user) => {
        setSelectedUsers(prev => {
            if (prev.includes(user)) {
                return prev.filter(u => u !== user);
            } else {
                return [...prev, user];
            }
        });
    };

    const handleView = () => {
        if (selectedUsers.length === 1) {
            setModalMode('view');
            setIsModalOpen(true);
        } else {
            alert("Please select exactly one record to view details.");
        }
    };

    const handleUpdate = () => {
        if (selectedUsers.length === 1) {
            setModalMode('update');
            setIsModalOpen(true);
        } else {
            alert("Please select exactly one record to update.");
        }
    };

    const handleAdd = async () => {
        const companyId = formData.serviceCdId ;

        setIsModalOpen(true);
        if (companyId) {
            try {
                const resp = await fetch(`http://192.168.0.108:8081/users/account-list/${companyId}`);
                if (!resp.ok) throw new Error(`HTTP error! status: ${resp.status}`);
                const accountData = await resp.json();
                console.log("Fetched Accounts:", accountData); 
                setAccounts(accountData);
                setModalMode('add');
                setSelectedUsers([]); // Clear selection for new user
                const initialFormData = {
                    serviceCdId: companyId,
                    activeFlag: 'Y',
                };
                setFormData(initialFormData);
                setIsModalOpen(true);
            } catch (err) {
                console.log("Error fetching accounts", err);
            }
        } else {
         
        }
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedUsers([]);
        setApprovers([]);
        setAccounts([]);
    };

    const handleSave = async (user) => {
        if (modalMode === 'add') {
            try {
                // Prepare the user object
                const approverDetailsToSave = approvers
                    .filter((approver) => approver.selected) // Assuming you track selected approvers
                    .map((approver) => `${approver.approverId}#${approver.dailyLimit}`); 
    
                const userData = {
                    serviceCdId: user.serviceCdId,
                    accountNo: user.accountNo,
                    groupName: user.groupName,
                    // id: 6046,
                    // authStatus: "New",
                    makerCd: "SME500",
                    makerDt: new Date().toISOString().split('T')[0],
                    makerCalDt: new Date().toISOString().split('T')[0],
                    activeFlag: user.activeFlag,
                    dailyLimit: user.dailyLimit,
                    serviceCd: 3,
                    approverList: approverDetailsToSave.join(","),
                };
    
                console.log("User Data to be sent:", userData);
    
                const resp = await fetch('http://192.168.0.108:8081/users/add', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(userData),
                });
    
                const textResponse = await resp.text();
                console.log("Response:", textResponse);
    
                if (!resp.ok) {
                    throw new Error(`HTTP error! status: ${resp.status}`);
                }
                
                const result = JSON.parse(textResponse);
                setData((prev) => [...prev, result]);
                handleCloseModal();
            } catch (err) {
                console.log("Error saving user", err);
            }
        } else if (modalMode === 'update') {
            // Handle update logic
        }
    };
    
    const handleAccountChange = async (companyID) => {
        try {
            const resp = await fetch(`http://192.168.0.108:8081/users/approver-list/${companyID}`);
            if (!resp.ok) throw new Error(`HTTP error! status: ${resp.status}`);
            const approverData = await resp.json();
            console.log("Fetched Approvers:", approverData);
             // Convert object to array
        const approversArray = Object.entries(approverData).map(([id, name]) => ({
            approverId: id,
            approverName: name
        }));

            setApprovers(approversArray);
            return approversArray;
        } catch (err) {
            console.log("Error fetching approvers", err);
            return []; // Return an empty array on error
        }
    };
    

    return (
        <div className="form-container">
            <h3>Single Go - Limit Maintenance Master - Maker</h3>
            <hr />
            <form>
                <div className="form-group">
                    <label htmlFor="companyID">Company ID</label>
                    <input
                        name="companyID"
                        id="companyID"
                        type="text"
                        value={searchInput.companyID}
                        onChange={handleInputChange}
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="accountNo">Account Number</label>
                    <input
                        name="accountNo"
                        id="accountNo"
                        type="text"
                        value={searchInput.accountNo}
                        onChange={handleInputChange}
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="groupName">Group Name</label>
                    <input
                        name="groupName"
                        id="groupName"
                        type="text"
                        value={searchInput.groupName}
                        onChange={handleInputChange}
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="status">Status</label>
                    <select
                        name="status"
                        id="status"
                        value={searchInput.status}
                        onChange={handleInputChange}
                    >
                        <option value="all">All</option>
                        <option value="Authorized">Authorized</option>
                        <option value="Modified">Modified</option>
                        <option value="New">New</option>
                    </select>
                </div>
                <div className="button-group">
                    <button type="button" onClick={handleSearch}>Search</button>
                    <button type="button" onClick={handleClear}>Clear</button>
                    <button type="button" onClick={handleView}>View</button>
                    <button type="button" onClick={handleAdd}>New</button>
                    <button type="button" onClick={handleUpdate}>Update</button>
                    <button type="button">Exit</button>
                </div>
            </form>

            <div className="table-container">
                <table>
                    <thead>
                        <tr>
                            <th></th>
                            <th>Company Id</th>
                            <th>Account Number</th>
                            <th>Group Name</th>
                            <th>Status</th>
                            <th>Active Flag</th>
                        </tr>
                    </thead>
                    <tbody>
                        {currentRecords.length > 0 ? (
                            currentRecords.map((item) => (
                                <tr key={item.id}>
                                    <td>
                                        <input
                                            type="checkbox"
                                            checked={selectedUsers.includes(item)}
                                            onChange={() => handleCheckboxChange(item)}
                                        />
                                    </td>
                                    <td>{item.serviceCdId}</td>
                                    <td>{item.accountNo}</td>
                                    <td>{item.groupName}</td>
                                    <td>{item.authStatus}</td>
                                    <td>{item.activeFlag}</td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="6">No results found</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>

            <div className="pagination">
                <button onClick={() => setCurrentPage(currentPage > 1 ? currentPage - 1 : currentPage)} disabled={currentPage === 1}>&lt; Prev</button>
                <span>Page {currentPage} of {totalPages}</span>
                <button onClick={() => setCurrentPage(currentPage < totalPages ? currentPage + 1 : currentPage)} disabled={currentPage === totalPages}>Next &gt;</button>
            </div>

            {isModalOpen && (
    <Modal
        mode={modalMode}
        user={selectedUsers.length === 1 ? selectedUsers[0] : null}
        onClose={handleCloseModal}
        onSave={handleSave}
        accounts={accounts}
        approvers={approvers}
        handleAccountChange={handleAccountChange} 
        handleAdd={handleAdd}
    />
)}
        </div>
    );
};

export default Form;
