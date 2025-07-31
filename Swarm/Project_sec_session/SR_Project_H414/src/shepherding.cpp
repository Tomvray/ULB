#include "shepherding.h"

#include <argos3/plugins/simulator/entities/cylinder_entity.h>

#include <algorithm>
#include <cstring>
#include <cerrno>
#include <string>

/****************************************/
/****************************************/

static const Real OBJECT_RADIUS            = 0.1f;
static const Real OBJECT_DIAMETER          = OBJECT_RADIUS * 2.0f;

static const Real CONSTRUCTION_AREA_MIN_X  = 2.69f;
static const Real CONSTRUCTION_AREA_MAX_X  = 3.69f;
static const Real CONSTRUCTION_AREA_MIN_Y  = -2.69f;
static const Real CONSTRUCTION_AREA_MAX_Y  = 2.69f;

/****************************************/
/****************************************/

CShepherding::CShepherding() :
   m_bResetAll(false),
   m_bAnytimeResult(false),
   m_fTargetMaxX(0),
   m_fTargetMinX(0),
   m_fTargetMaxY(0),
   m_fTargetMinY(0),
   m_pcRNG(NULL) {
}

/****************************************/
/****************************************/

CShepherding::~CShepherding() {
   /* Nothing to do */
}

/****************************************/
/****************************************/

void CShepherding::Init(TConfigurationNode& t_tree) {
   try {
      TConfigurationNode& tParams = GetNode(t_tree, "params");

      /* Get the cache area configuration from XML */
      GetNodeAttribute(tParams, "reset_all", m_bResetAll);
      GetNodeAttribute(tParams, "anytime_result", m_bAnytimeResult);
      GetNodeAttribute(tParams, "target_max_x", m_fTargetMaxX);
      GetNodeAttribute(tParams, "target_min_x", m_fTargetMinX);
      GetNodeAttribute(tParams, "target_max_y", m_fTargetMaxY);
      GetNodeAttribute(tParams, "target_min_y", m_fTargetMinY);
      
   }
   catch(CARGoSException& ex) {
      THROW_ARGOSEXCEPTION_NESTED("Error parsing loop functions!", ex);
   }

   m_pcRNG = CRandom::CreateRNG("argos");

}

/****************************************/
/****************************************/

void CShepherding::Reset() {
   if (m_bResetAll)
   {
      /* Nothing to do */
   }
}

/****************************************/
/****************************************/

void CShepherding::Destroy() {
   /* Nothing to do */
}

/****************************************/
/****************************************/

void CShepherding::PreStep() {
   /* Nothing to do */
}

/****************************************/
/****************************************/

void CShepherding::PostStep() {
  if (m_bAnytimeResult) {
    /* Compute score at every time step */
    ComputeScore();
  }
  

}

/****************************************/
/****************************************/

void CShepherding::PostExperiment() {
  ComputeScore();
}

/****************************************/
/****************************************/

CColor CShepherding::GetFloorColor(const CVector2& c_position_on_plane) {
   return CColor::WHITE;
}

/****************************************/
/****************************************/

void CShepherding::ComputeScore() {
  UInt32 score = 0;
  CFootBotEntity* pcFootBot;
  CSpace::TMapPerType& tFootBotMap = GetSpace().GetEntitiesByType("foot-bot");
  for (CSpace::TMapPerType::iterator it = tFootBotMap.begin(); it != tFootBotMap.end(); ++it) {
    pcFootBot = any_cast<CFootBotEntity*>(it->second);
    /* Check if it is a sheep */
    if (pcFootBot->GetId().find("sheep") != std::string::npos) {
      if ((pcFootBot->GetEmbodiedEntity().GetOriginAnchor().Position.GetX() < m_fTargetMaxX) && (pcFootBot->GetEmbodiedEntity().GetOriginAnchor().Position.GetX() > m_fTargetMinX) &&
          (pcFootBot->GetEmbodiedEntity().GetOriginAnchor().Position.GetY() < m_fTargetMaxY) && (pcFootBot->GetEmbodiedEntity().GetOriginAnchor().Position.GetY() > m_fTargetMinY)) {
        score++;
      }
    }
  }
  /* Output the score for this step */
  LOG << "Score: " << score << std::endl;
}

/****************************************/
/****************************************/

void CShepherding::MoveRobots() {
  CFootBotEntity* pcFootBot;
  bool bPlaced = false;
  UInt32 unTrials;
  CSpace::TMapPerType& tFootBotMap = GetSpace().GetEntitiesByType("foot-bot");
  for (CSpace::TMapPerType::iterator it = tFootBotMap.begin(); it != tFootBotMap.end(); ++it) {
    pcFootBot = any_cast<CFootBotEntity*>(it->second);
    // Choose position at random
    unTrials = 0;
    do {
       ++unTrials;
       CVector3 cFootBotPosition = GetRandomRobotPosition();
       bPlaced = MoveEntity(pcFootBot->GetEmbodiedEntity(),
                            cFootBotPosition,
                            CQuaternion().FromEulerAngles(m_pcRNG->Uniform(CRange<CRadians>(CRadians::ZERO,CRadians::TWO_PI)),
                            CRadians::ZERO,CRadians::ZERO),false);
    } while(!bPlaced && unTrials < 1000);
    if(!bPlaced) {
       THROW_ARGOSEXCEPTION("Can't place robot");
    }
  }
}

/****************************************/
/****************************************/

CVector3 CShepherding::GetRandomRobotPosition() {
  Real temp;
  Real fPoseX = m_pcRNG->Uniform(CRange<Real>(-2.0f, 2.0f));
  Real fPoseY = m_pcRNG->Uniform(CRange<Real>(-3.0f, 3.0f));

  return CVector3(fPoseX, fPoseY, 0);
}

/****************************************/
/****************************************/

/* Register this loop functions into the ARGoS plugin system */
REGISTER_LOOP_FUNCTIONS(CShepherding, "shepherding");
