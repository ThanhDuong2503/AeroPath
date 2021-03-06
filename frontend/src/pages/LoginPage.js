import React, {useContext, useState} from "react";
import {
    UserDispatchContext,
    UserStateContext,
} from "../context/user/UserContext";
import {
    LOGIN,
    LOGIN_FAILED,
    LOGIN_SUCCESS,
} from "../context/user/UserContextProvider";
import {performLogin} from "../utils/auth-utils";
import {Link, Redirect, useLocation} from "react-router-dom";
import {getDecodedJWTToken, setJWTToken} from "../utils/jwt-utils";
import {Grid, makeStyles} from '@material-ui/core';
import "./LoginPage.css";
import ListAltIcon from '@material-ui/icons/ListAlt';
import VpnKeyOutlinedIcon from '@material-ui/icons/VpnKeyOutlined';
import {GithubLoginButton} from "../components/oauth/GithubLoginButton";
import LoginPageButton from "../components/LoginPageButtons/LoginPageButton";


const useStyles = makeStyles((theme) => ({
    root: {
        flexGrow: 1,
    },
    gridContainer: {
        paddingTop: theme.spacing(3),
        textAlign: "center",
        maxHeight: 100,
    },
}));


function LoginPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const dispatch = useContext(UserDispatchContext);

    const classes = useStyles();

    function login() {
        dispatch({type: LOGIN});
        performLogin(username, password)
            .then((data) => {
                setJWTToken(data);
                const userData = getDecodedJWTToken();
                dispatch({type: LOGIN_SUCCESS, payload: userData});
            })
            .catch(() => {
                dispatch({type: LOGIN_FAILED});
            });
    }

    const {authStatus} = useContext(UserStateContext);
    const location = useLocation();
    if (authStatus === "SUCCESS") {
        const locationState = location.state || {from: {pathname: "/"}}
        return <Redirect to={locationState.from.pathname}/>;
    }

    return (
        <div className="background-image">
            <div className={classes.root}>
                <Grid container
                      className={classes.gridContainer}
                      justify="center"
                >
                    <Grid item className="loginStyle">
                        <img src="images/AeroPathLogo.png" alt="appLogo"/>

                        <Grid container spacing={2} direction={"column"} alignContent={"center"}>

                            <Grid item xs={10}>
                                <input
                                    className="loginInputField"
                                    type="text"
                                    placeholder="Username"
                                    value={username}
                                    onChange={(event) => setUsername(event.target.value)}
                                />

                                <input
                                    className="loginInputField"
                                    type="password"
                                    placeholder="Password"
                                    value={password}
                                    onChange={(event) => setPassword(event.target.value)}
                                />
                            </Grid>
                            <br/>
                            <Grid item xs={10}>
                                <LoginPageButton onClickAction={login}
                                                 buttonIcon={<VpnKeyOutlinedIcon/>}
                                                 buttonName={"Login"}/>
                            </Grid>

                            <Grid item xs={10}>
                                <Link to="/registration" style={{textDecoration: "none"}}>
                                    <LoginPageButton buttonIcon={<ListAltIcon/>}
                                                     buttonName={"Register"}/>
                                </Link>
                            </Grid>

                            <Grid item xs={10}>
                                <p>- or -</p>
                            </Grid>

                            <Grid item xs={10}>
                                <GithubLoginButton/>
                            </Grid>

                            <Grid item xs={10}>
                                <a href={"https://github.com/ThanhDuong2503/AeroPath"}
                                   style={{textDecoration: "none", color : "#3eb0d4"}}
                                >© by Thanh Duong</a>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </div>
        </div>
    );
}

export default LoginPage;
