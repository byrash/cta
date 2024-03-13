import React, {useEffect, useState} from "react";
import TextField from '@mui/material/TextField';


function MultiSigner(props: any) {
    const [signersData, setSignersData] = React.useState([
        [
            {
                label: "Order",
                field: "order",
                value: "",
                name: "00"
            },
            {
                label: "Signer Name",
                field: "signerName",
                value: "",
                name: "01"
            },
            {
                label: "Signer Email",
                field: "signerEmail",
                value: "",
                name: "02"
            }
            ,
            {
                label: "Role",
                field: "role",
                value: "",
                name: "03"
            }
        ]
    ]);
    const [filledFormId, setFilledFormID] = useState<string>()

    useEffect(() => {
        setFilledFormID(props.filledFormId)
    }, [props.filledFormId])

    const handleOnChange = (e: any, row: any, col: any) => {
        const newData = signersData.map((d, i) => {
            if (i === row) {
                d[col].value = e.target.value;
            }
            return d;
        });
        setSignersData(newData);
    };

    const addRow = () => {
        // console.log(signersData);
        setSignersData([
            ...signersData,
            [
                {
                    label: "Order",
                    field: "order",
                    value: "",
                    name: `${signersData.length}0`
                },
                {
                    label: "Signer Name",
                    field: "signerName",
                    value: "",
                    name: `${signersData.length}1`
                },
                {
                    label: "Signer Email",
                    field: "signerEmail",
                    value: "",
                    name: `${signersData.length}2`
                },
                {
                    label: "Role",
                    field: "role",
                    value: "",
                    name: `${signersData.length}3`
                }
            ]
        ]);
    };

    const removeRow = (index: number) => {
        const _data = [...signersData];
        _data.splice(index, 1);
        setSignersData(_data);
    };

    function handleSubmit(event: any) {
        event.preventDefault()
        // console.log(signersData)
        // console.log(props.filledFormId)
        // console.log(props.filledFormURL)
        let data: object[] = [];
        signersData.map((items, i) => {
            let order, name, email, role;
            items.map((item, i1) => {
                if (item.field === 'order') {
                    order = item.value;
                } else if (item.field === 'signerName') {
                    name = item.value;
                } else if (item.field === 'signerEmail') {
                    email = item.value;
                } else if (item.field === 'role') {
                    role = item.value;
                }
            })
            if (order && name && email) {
                data.push({order, name, email, role})
            }
        })
        // @ts-ignore
        data.sort((a, b) => a.order - b.order)
        // console.log(data)
        let groupedData: object[][] = [[]]
        data.map((entry, i) => {
            // @ts-ignore
            if (!groupedData[entry.order]) {
                // @ts-ignore
                groupedData[entry.order] = []
            }
            // @ts-ignore
            groupedData[entry.order].push(entry)
        })
        groupedData = groupedData.filter(value => value != null && value.length > 0)
        // console.log(groupedData)
        let submitData = {filledFormId: props.filledFormId, filledFormURL: props.filledFormURL, signers: groupedData}
        console.log(submitData)
        // const config = {
        //     headers: {
        //         'content-type': 'application/json',
        //         'Access-Control-Allow-Origin': '*',
        //     },
        // };
        // const url = 'http://localhost:8080/api/data/' + props.schema.templateId;
        // axios.post(url, signersData, config).then((response) => {
        //     // FileDownload(response.signersData, 'filled_form', "application/pdf");
        //     let filledFormURL = 'http://localhost:8080/api/cta/' + response.signersData;
        //     props.onSubmission(response.signersData)
        //     // window.open(filledFormURL)
        // }).catch(reason => console.log(reason));
    }

    return (
        <span>
        {
            filledFormId && (
                <div className="container">
                    {signersData.map((items, i1) => (
                        <div key={i1} className="content">
                            <br/>
                            <div className="content-row">
                                {items.map((item, i2) => (
                                    <TextField
                                        key={i2}
                                        label={item.label}
                                        value={item.value}
                                        onChange={(e) => handleOnChange(e, i1, i2)}
                                        variant="outlined"
                                        name={item.name}
                                    />
                                ))}
                                <button onClick={addRow}>+</button>
                                {signersData.length > 1 && (
                                    <button onClick={() => removeRow(i1)}>-</button>
                                )}
                            </div>
                        </div>
                    ))}
                    <br/><br/>
                    <button onClick={handleSubmit}>Start CTA Ceremony</button>
                </div>
            )
        }
        </span>
    );
}

export default MultiSigner;