import React from "react";
import TextField from '@mui/material/TextField';


function MultiSigner(props: any) {
    const [data, setData] = React.useState([
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

    const handleOnChange = (e: any, row: any, col: any) => {
        const newData = data.map((d, i) => {
            if (i === row) {
                d[col].value = e.target.value;
            }

            return d;
        });
        setData(newData);
    };

    const addRow = () => {
        console.log(data);
        setData([
            ...data,
            [
                {
                    label: "Order",
                    field: "order",
                    value: "",
                    name: `${data.length}0`
                },
                {
                    label: "Signer Name",
                    field: "signerName",
                    value: "",
                    name: `${data.length}1`
                },
                {
                    label: "Signer Email",
                    field: "signerEmail",
                    value: "",
                    name: `${data.length}2`
                }
            ]
        ]);
    };

    const removeRow = (index: number) => {
        const _data = [...data];
        _data.splice(index, 1);
        setData(_data);
    };

    return (
        <div className="container">
            {data.map((items, i1) => (
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
                        {data.length > 1 && (
                            <button onClick={() => removeRow(i1)}>-</button>
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
};

export default MultiSigner;