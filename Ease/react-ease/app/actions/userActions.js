var api =require('../utils/api');
var post_api = require('../utils/post_api');

export function selectTeamUser(id){
  return function(dispatch, getState){
    dispatch({type: 'SELECT_USER_PENDING'});
    return api.fetchTeamUser(getState().team.id, id).then(response => {
      var teamUser = response;
      api.fetchTeamUserApps(getState().team.id, id).then(response => {
        teamUser.apps = response;
        dispatch({type: 'SELECT_USER_FULFILLED', payload: teamUser});
      });
    }).catch(err => {
      dispatch({type:'SELECT_USER_REJECTED', payload:err});
      throw err;
    });
  }
}

export function fetchUsers(team_id){
  return function(dispatch, getState){
    dispatch({type: 'FETCH_USERS_PENDING'});
    return api.fetchTeamUsers(team_id).then((response) => {
      var myTeamUserId = getState().team.myTeamUserId;
      var payload = {
        users : response,
        myTeamUserId : myTeamUserId
      };
      dispatch({type: "FETCH_USERS_FULFILLED", payload: payload});
    }).catch((err) => {
      dispatch({type:"FETCH_USERS_REJECTED", payload: err});
      throw err;
    })
  }
}

export function getTeamUserShareableApps(team_user_id){
  return function(dispatch, getState){
    dispatch({type: 'GET_TEAM_USER_SHAREABLE_APPS_PENDING'});
    return api.fetchTeamUserShareableApps(getState().team.id, team_user_id).then(response => {
      dispatch({type: 'GET_TEAM_USER_SHAREABLE_APPS_FULFILLED', payload:{team_user_id:team_user_id, apps:response}});
      return response;
    }).catch(err => {
      dispatch({type: 'GET_TEAM_USER_SHAREABLE_APPS_REJECTED'});
      throw err;
    });
  }
}

export function createTeamUser(first_name, last_name, email, username, departure_date, role){
  return function(dispatch, getState){
    dispatch({type:'CREATE_TEAM_USER_PENDING'});
    return post_api.teamUser.createTeamUser(getState().team.id, first_name, last_name, email, username, departure_date, role).then(response => {
      dispatch({type: 'CREATE_TEAM_USER_FULFILLED', payload: response});
      return response;
    }).catch(err => {
      dispatch({type: 'CREATE_TEAM_USER_REJECTED'});
      throw err;
    });
  }
}

export function deleteTeamUser(team_user_id){
  return function(dispatch, getState){
    dispatch({type: 'DELETE_TEAM_USER_PENDING'});
    return post_api.teamUser.deleteTeamUser(getState().team.id, team_user_id).then(response => {
      //need to reselect existing user
      const selection = getState().selection;
      const users = getState().users.users;
      var userToSelect = null;

      for (var i = 0; i < users.length; i++){
        if (users[i].id != team_user_id){
          userToSelect = users[i];
          break;
        }
      }
      if (userToSelect && selection.type === 'user' && selection.item.id === team_user_id){
        return dispatch(selectTeamUser(userToSelect.id)).then(() => {
          return dispatch({type: 'DELETE_TEAM_USER_FULFILLED', payload:{team_user_id: team_user_id}});
        });
      }else
        return dispatch({type: 'DELETE_TEAM_USER_FULFILLED', payload:{team_user_id: team_user_id}});
    }).catch(err => {
      dispatch({type: 'DELETE_TEAM_USER_REJECTED', payload: err});
      throw err;
    })
  }
}

export function editTeamUserUsername(user_id, username){
  return function(dispatch, getState){
    dispatch({type: 'EDIT_TEAM_USER_USERNAME_PENDING'});
    return post_api.teamUser.editUsername(getState().team.id, user_id, username).then(response => {
      dispatch({type:'EDIT_TEAM_USER_USERNAME_FULFILLED', payload: {id: user_id, username: username}});
    }).catch(err => {
      dispatch({type: 'EDIT_TEAM_USER_USERNAME_REJECTED', payload: err});
      throw err;
    })
  }
}

export function editTeamUserFirstName(user_id, first_name){
  return function(dispatch, getState){
    dispatch({type: 'EDIT_TEAM_USER_FIRSTNAME_PENDING'});
    return post_api.teamUser.editFirstName(getState().team.id, user_id, first_name).then(response => {
      dispatch({type:'EDIT_TEAM_USER_FIRSTNAME_FULFILLED', payload: {id: user_id, first_name: first_name}});
    }).catch(err => {
      dispatch({type: 'EDIT_TEAM_USER_FIRSTNAME_REJECTED', payload: err});
      throw err;
    })
  }
}

export function editTeamUserLastName(user_id, last_name){
  return function(dispatch, getState){
    dispatch({type: 'EDIT_TEAM_USER_LASTNAME_PENDING'});
    return post_api.teamUser.editLastName(getState().team.id, user_id, last_name).then(response => {
      dispatch({type:'EDIT_TEAM_USER_LASTNAME_FULFILLED', payload: {id: user_id, last_name: last_name}});
    }).catch(err => {
      dispatch({type: 'EDIT_TEAM_USER_LASTNAME_REJECTED', payload: err});
      throw err;
    })
  }
}

export function editTeamUserRole(user_id, role){
  return function(dispatch, getState){
    dispatch({type: 'EDIT_TEAM_USER_ROLE_PENDING'});
    return post_api.teamUser.editRole(getState().team.id, user_id, role).then(response => {
      dispatch({type:'EDIT_TEAM_USER_ROLE_FULFILLED', payload: {id: user_id, role: role}});
    }).catch(err => {
      dispatch({type: 'EDIT_TEAM_USER_ROLE_REJECTED', payload: err});
      throw err;
    })
  }
}

export function editTeamUserDepartureDate(user_id, departure_date){
  return function(dispatch, getState){
    dispatch({type: 'EDIT_TEAM_USER_DEPARTUREDATE_PENDING'});
    return post_api.teamUser.editDepartureDate(getState().team.id, user_id, departure_date).then(response => {
      dispatch({type:'EDIT_TEAM_USER_DEPARTUREDATE_FULFILLED', payload: {id: user_id, departure_date: departure_date}});
    }).catch(err => {
      dispatch({type: 'EDIT_TEAM_USER_DEPARTUREDATE_REJECTED', payload: err});
      throw err;
    })
  }
}