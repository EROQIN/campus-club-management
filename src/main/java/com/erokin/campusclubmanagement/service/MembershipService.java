package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.membership.MembershipAdminResponse;
import com.erokin.campusclubmanagement.dto.membership.MembershipDecisionRequest;
import com.erokin.campusclubmanagement.dto.membership.MembershipRequest;
import com.erokin.campusclubmanagement.dto.membership.MembershipResponse;
import java.util.List;

public interface MembershipService {

    MembershipResponse apply(MembershipRequest request);

    MembershipResponse decide(Long membershipId, MembershipDecisionRequest request);

    void withdraw(Long membershipId);

    List<MembershipResponse> listMyMemberships();

    List<MembershipResponse> listClubApplicants(Long clubId);

    List<MembershipAdminResponse> listClubMembers(Long clubId);
}
