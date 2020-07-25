import {fetchAllWaypoints, putWaypoint, deleteWaypoint} from "../../utils/waypoints-utils";

export const FETCH_WAYPOINTS = 'FETCH_WAYPOINTS';
export const FETCH_WAYPOINTS_SUCCESS = 'FETCH_WAYPOINTS_SUCCESS';
export const FETCH_WAYPOINTS_FAILED = 'FETCH_WAYPOINTS_FAILED';
export const ADD_WAYPOINT = 'ADD_WAYPOINT';
export const ADD_WAYPOINT_SUCCESS = 'ADD_WAYPOINT_SUCCESS';
export const ADD_WAYPOINT_FAILED = 'ADD_WAYPOINT_FAILED';
export const DELETE_WAYPOINT = 'DELETE_WAYPOINT';
export const DELETE_WAYPOINT_SUCCESS = 'DELETE_WAYPOINT_SUCCESS';
export const DELETE_WAYPOINT_FAILED = 'DELETE_WAYPOINT_FAILED';

export async function fetchWaypoints(dispatch) {
    dispatch({ type: FETCH_WAYPOINTS });
    try {
        const waypoints = await fetchAllWaypoints();
        dispatch({ type: FETCH_WAYPOINTS_SUCCESS, payload: waypoints });
    } catch (error) {
        dispatch({ type: FETCH_WAYPOINTS_FAILED, payload: error });
    }
}

export async function addWaypoint(dispatch, latitude, longitude) {
    dispatch({ type: ADD_WAYPOINT });
    try {
        console.log("test")
        const waypoint = await putWaypoint(latitude, longitude);
        dispatch({ type: ADD_WAYPOINT_SUCCESS, payload: waypoint });
    } catch (error) {
        dispatch({ type: ADD_WAYPOINT_FAILED, payload: error });
    }
}

export async function removeWaypoint(dispatch, id) {
    dispatch({ type: DELETE_WAYPOINT });
    try {
        await deleteWaypoint(id);
        dispatch({ type: DELETE_WAYPOINT_SUCCESS, payload: id });
    } catch (error) {
        dispatch({ type: DELETE_WAYPOINT_FAILED });
    }
}