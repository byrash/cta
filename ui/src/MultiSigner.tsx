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
        console.log(signersData);
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
        console.log(signersData)
        console.log(props.filledFormId)
        console.log(props.filledFormURL)
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
            filledFormId && (<form onSubmit={handleSubmit}>
                <div className="container">
                    {signersData.map((items, i1) => (
                        <div key={i1} className="content">
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
                            </div>
                            <div>
                                <button onClick={addRow}>+</button>
                                {signersData.length > 1 && (
                                    <button onClick={() => removeRow(i1)}>-</button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
                <button type="submit">Start CTA Ceremony</button>
            </form>)
        }
        </span>
    );
}

export default MultiSigner;